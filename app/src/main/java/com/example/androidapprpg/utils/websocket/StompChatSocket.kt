package com.example.androidapprpg.utils.websocket

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONArray
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Singleton

@Singleton
class StompChatSocket(
    private val ok: OkHttpClient,
    private val gson: Gson,
    private val base: String,
    private val stage: String?,
    private val endpoint: String,
    private val useSockJs: Boolean,
    var logBodies: Boolean = true
) {

    private object WS {
        object topics {
            fun chat(id: Any) = "/topic/chat.${id}"
        }
        object app {
            fun chatSend(id: Any) = "/app/chat.${id}.message"
        }
    }

    companion object {
        private const val TAG = "STOMP"
        private fun t(sub: String) = "$TAG/$sub"
    }

    private var ws: WebSocket? = null

    private val isActive     = AtomicBoolean(false)   // só true depois de CONNECTED
    private val connecting   = AtomicBoolean(false)   // estamos em processo de conectar
    private val sockJsOpened = AtomicBoolean(false)   // já recebemos "o"

    private val pendingSubs  = ConcurrentLinkedQueue<String>()
    private val pendingSends = ConcurrentLinkedQueue<Triple<String, String, Map<String, String>>>()

    var sockJsUseNullTerminator: Boolean = true

    private lateinit var wsUrl: String

    @Volatile private var connectHeaders: Map<String, String> = emptyMap()
    @Volatile private var messageCallback: ((String) -> Unit)? = null

    // -------------------- API pública --------------------

    fun setAuth(headers: Map<String, String>) {
        connectHeaders = headers
        Log.d(t("AUTH"), "setAuth headers=${redact(headers)}")
    }

    fun onMessage(listener: (String) -> Unit) {
        Log.d(t("CB"), "onMessage(): listener registrado")
        messageCallback = listener
    }

    fun connect() {
        if (!connecting.compareAndSet(false, true)) return

        wsUrl = buildWsUrl()

        if (!useSockJs) {
            openWebSocket()
            return
        }

        warmUpSockJsCookieAsync {
            openWebSocket()
        }
    }

    fun connectIfNeeded() {
        if (!isActive.get() && !connecting.get()) {
            connect()
        }
    }

    fun isConnected(): Boolean = isActive.get()

    fun disconnect() {
        Log.i(t("CONNECT"), "disconnect() called")
        isActive.set(false)
        connecting.set(false)
        sockJsOpened.set(false)
        ws?.close(1000, "bye")
        ws = null
    }

    fun subscribe(chatId: String, onMessage: (String) -> Unit): () -> Unit {
        messageCallback = onMessage

        val destination = WS.topics.chat(chatId)
        val subId = "sub-${destination.trimStart('/')}"
        val cmd = buildSubscribe(destination, subId)

        if (isActive.get()) {
            sendRaw(cmd)
        } else {
            pendingSubs.add(cmd)
            Log.d(t("SUB"), "socket ainda não CONNECTED -> SUB pendente $destination")
        }

        return {
            runCatching {
                sendRaw(buildUnsubscribe(subId))
            }.onFailure { e ->
                Log.w(t("SUB"), "unsubscribe falhou: ${e.message}", e)
            }
        }
    }

    fun sendChat(chatId: String, body: Any, headers: Map<String, String> = emptyMap()) {
        val destination = WS.app.chatSend(chatId)
        send(destination, body, headers)
    }

    fun send(destination: String, body: Any, headers: Map<String, String> = emptyMap()) {
        val jsonBody = if (body is String) body else gson.toJson(body)
        val contentType = headers["content-type"] ?: "application/json;charset=UTF-8"
        val frame = buildSend(destination, contentType, jsonBody, headers - "content-type")

        if (isActive.get()) {
            if (logBodies) Log.v(t("SEND"), "body=$jsonBody")
            sendRaw(frame)
        } else {
            pendingSends.add(Triple(destination, jsonBody, headers))
            Log.d(t("SEND"), "AINDA NÃO CONNECTED -> SEND pendente $destination")
        }
    }

    // -------------------- conexão física WS --------------------

    private fun openWebSocket() {
        val req = Request.Builder()
            .url(wsUrl)
            .header("Origin", "https://main.d3r5mqem6d9ler.amplifyapp.com")
            .build()

        ws = ok.newWebSocket(req, socketListener)
        Log.i(t("CONNECT"), "Connecting to $wsUrl (sockJs=$useSockJs)")
    }

    private fun warmUpSockJsCookieAsync(done: () -> Unit) {
        val infoUrl = base.toHttpUrl().newBuilder()
            .addPathSegment(sanitizeEndpoint(endpoint)) // "ws"
            .addPathSegment("info")
            .addQueryParameter("_", System.currentTimeMillis().toString())
            .build()

        ok.newCall(Request.Builder().url(infoUrl).get().build())
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.w(t("CONNECT"), "préflight /ws/info falhou: ${e.message}")
                    done()
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d(t("CONNECT"), "preflight /ws/info -> ${response.code}")
                    response.close()
                    done()
                }
            })
    }

    // -------------------- WebSocketListener --------------------

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(t("WS"), "onOpen code=${response.code} url=$wsUrl")

            if (!useSockJs) {
                // caminho WS puro (não usado no teu backend atual)
                sendRaw(buildNativeConnectFrame())
            } else {
                // SockJS handshake: servidor manda "o" primeiro
                sockJsOpened.set(false)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (useSockJs) {
                when {
                    text == "o" -> {
                        Log.v(t("WS"), "SockJS OPEN (o)")
                        if (sockJsOpened.compareAndSet(false, true)) {
                            // >>> IMPORTANTE <<<
                            // SockJS cliente -> servidor NÃO manda "a[...]".
                            // Ele manda só ["..."].
                            // Então aqui a gente usa wrapSockJsClient().
                            val connectPayload = buildNativeConnectFrame()
                            val sockJsFrame = wrapSockJsClient(connectPayload)
                            sendRaw(sockJsFrame)
                        }
                        return
                    }
                    text == "h" -> {
                        Log.v(t("WS"), "SockJS heartbeat (h)")
                        return
                    }
                    text.startsWith("a[") -> {
                        // servidor -> cliente, sempre vem começando com "a["
                        parseSockJsServerFrame(text).forEach { handleStompFrame(it) }
                        return
                    }
                    text.startsWith("c[") -> {
                        isActive.set(false)
                        connecting.set(false)
                        sockJsOpened.set(false)
                        Log.w(t("WS"), "SockJS CLOSE recebido: $text")
                        return
                    }
                }
            }

            // fallback STOMP puro (sem SockJS)
            handleStompFrame(text)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.w(t("WS"), "onClosed code=$code reason=$reason")
            isActive.set(false)
            connecting.set(false)
            sockJsOpened.set(false)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(t("WS"), "onFailure: ${t.message}", t)
            response?.let {
                Log.e(t("WS"), "resp=${it.code} ${it.message}")
            }
            isActive.set(false)
            connecting.set(false)
            sockJsOpened.set(false)
        }
    }

    // -------------------- STOMP frame handling --------------------

    private fun handleStompFrame(raw: String) {
        raw.split('\u0000').forEach { part ->
            val frame = part.trim()
            if (frame.isEmpty() || !connecting.get()) return@forEach

            val command = frame.substringBefore('\n', frame)

            when {
                command.startsWith("CONNECTED") -> {
                    Log.i(t("STOMP"), "CONNECTED recebido 🎉")
                    isActive.set(true)

                    // drena SUBSCRIBEs pendentes
                    var drainedSubs = 0
                    while (true) {
                        val s = pendingSubs.poll() ?: break
                        drainedSubs++
                        sendRaw(s)
                    }
                    Log.d(t("STOMP"), "SUBSCRIBEs drenados: $drainedSubs")

                    // drena SENDs pendentes
                    var drainedSends = 0
                    while (true) {
                        val p = pendingSends.poll() ?: break
                        drainedSends++
                        val ct = p.third["content-type"] ?: "application/json;charset=UTF-8"
                        val send = buildSend(p.first, ct, p.second, p.third - "content-type")
                        sendRaw(send)
                    }
                    Log.d(t("STOMP"), "SENDs drenados: $drainedSends")
                }

                command.startsWith("MESSAGE") -> {
                    val body = frame.substringAfter("\n\n", "")
                    if (logBodies) Log.v(t("MSG"), "body=$body")
                    try {
                        messageCallback?.invoke(body)
                    } catch (e: Throwable) {
                        Log.e(t("MSG"), "callback error: ${e.message}", e)
                    }
                }

                command.startsWith("ERROR") -> {
                    Log.e(t("STOMP"), "ERROR frame: ${preview(frame)}")
                }

                command.startsWith("RECEIPT") -> {
                    Log.v(t("STOMP"), "RECEIPT frame: ${preview(frame)}")
                }

                else -> {
                    Log.v(t("STOMP"), "FRAME outro: ${preview(frame)}")
                }
            }
        }
    }

    // -------------------- STOMP frames builders --------------------

    /**
     * Frame CONNECT nativo STOMP.
     * Esse texto ENTROU dentro do array JSON ["..."] que mandamos pro SockJS servidor.
     *
     * Importante:
     * - accept-version:1.1,1.0
     * - heart-beat:10000,10000
     * - NÃO vamos colocar "host:" porque não é obrigatório pro Spring SimpleBroker.
     * - linha em branco
     * - terminador '\u0000'
     */
    private fun buildNativeConnectFrame(): String {
        val sb = StringBuilder()
        sb.append("CONNECT\n")
        sb.append("accept-version:1.1,1.0\n")
        sb.append("heart-beat:10000,10000\n")

        // headers extras tipo Authorization se precisar
        connectHeaders.forEach { (k, v) ->
            sb.append(k).append(':').append(v).append('\n')
        }

        sb.append("\n")
        sb.append('\u0000')
        return sb.toString()
    }

    private fun buildSubscribe(destination: String, id: String): String =
        stompFrame(
            command = "SUBSCRIBE",
            headers = mapOf(
                "id" to id,
                "destination" to destination,
                "ack" to "auto"
            ),
            body = null
        )

    private fun buildUnsubscribe(id: String): String =
        stompFrame(
            command = "UNSUBSCRIBE",
            headers = mapOf("id" to id),
            body = null
        )

    private fun buildSend(
        destination: String,
        contentType: String,
        body: String,
        extraHeaders: Map<String, String> = emptyMap()
    ): String =
        stompFrame(
            command = "SEND",
            headers = mapOf(
                "destination" to destination,
                "content-type" to contentType
            ) + extraHeaders,
            body = body
        )

    /**
     * Monta um frame STOMP genérico e depois:
     * - se SockJS=true, embrulha em um JSON array ["..."] (SEM 'a'!)
     * - se SockJS=false, retorna o frame cru
     *
     * OBS: cada frame STOMP termina com \u0000 se sockJsUseNullTerminator=true
     */
    private fun stompFrame(
        command: String,
        headers: Map<String, String>,
        body: String?
    ): String {
        val sb = StringBuilder().apply {
            append(command).append('\n')
            headers.forEach { (k, v) ->
                append(k).append(':').append(v).append('\n')
            }
            append('\n')
            if (body != null) append(body)

            val needsNull = if (useSockJs) sockJsUseNullTerminator else true
            if (needsNull) append('\u0000')
        }

        val rawFrame = sb.toString()
        return if (useSockJs) {
            wrapSockJsClient(rawFrame)
        } else {
            rawFrame
        }
    }

    // -------------------- SockJS helpers --------------------

    private fun buildWsUrl(): String {
        val ep = sanitizeEndpoint(endpoint) // "ws"

        val httpUrl = base.toHttpUrl().newBuilder().apply {
            addPathSegments(ep)

            stage?.let { st ->
                if (st.isNotBlank()) {
                    addPathSegment(st.trim('/'))
                }
            }

            if (useSockJs) {
                val serverId = "%03d".format((0..999).random())
                val sid = UUID.randomUUID().toString().replace("-", "")
                addPathSegment(serverId)
                addPathSegment(sid)
                addPathSegment("websocket")
            }
        }.build().toString()

        return when {
            httpUrl.startsWith("https://", ignoreCase = true) ->
                httpUrl.replaceFirst("https://", "wss://")
            httpUrl.startsWith("http://", ignoreCase = true) ->
                httpUrl.replaceFirst("http://", "ws://")
            else -> httpUrl
        }
    }

    private fun sanitizeEndpoint(raw: String): String = raw.trim().trim('/')

    /**
     * >>> CLIENTE → SERVIDOR <<<
     * O cliente SockJS envia APENAS um array JSON. Ex: ["CONNECT....\u0000"]
     * NÃO manda prefixo "a".
     */
    private fun wrapSockJsClient(payload: String): String {
        val out = JSONArray().put(payload).toString()
        if (logBodies) {
            Log.v(t("WS"), "SockJS CLIENT OUT literal=${out.debugLiteral()}")
        }
        return out
    }

    /**
     * >>> SERVIDOR → CLIENTE <<<
     * O servidor SockJS manda "a[\"...\"]"
     * Aqui a gente desembrulha isso e retorna a lista de frames STOMP.
     */
    private fun parseSockJsServerFrame(text: String): List<String> {
        // text tipo: a["MESSAGE\nsubscription:..."]
        val inner = text.substringAfter("a[", "").removeSuffix("]")
        if (inner.isBlank()) return emptyList()

        return inner
            .split("\",\"")
            .map {
                it.trim('"')
                    .replace("\\n", "\n")
                    .replace("\\u0000", "\u0000")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
            }
    }

    // -------------------- envio bruto WS --------------------

    private fun sendRaw(frame: String) {
        if (logBodies) {
            val lit = frame.debugLiteral()
            val bytes = frame.toByteArray(Charsets.UTF_8)
            val lastChar = frame.lastOrNull()
            Log.v(t("WIRE"), "OUT >>> literal=$lit")
            Log.v(t("WIRE"), "OUT >>> lenChars=${frame.length} lenBytes=${bytes.size} lastChar=$lastChar lastCode=${lastChar?.code}")
        } else {
            Log.v(t("WIRE"), "OUT >>> ${preview(frame)}")
        }

        val ok = ws?.send(frame) ?: false
        if (!ok) {
            Log.w(t("WIRE"), "sendRaw falhou (ws nulo/fechado)")
        }
    }

    // -------------------- utils --------------------

    private fun redact(headers: Map<String, String>): Map<String, String> =
        headers.mapValues { (k, v) ->
            if (k.equals("authorization", true)) "***REDACTED***" else v
        }

    private fun preview(s: String, max: Int = 200): String =
        if (s.length <= max) s else s.substring(0, max) + "…(+${s.length - max})"

    private fun String.debugLiteral(): String = buildString {
        for (ch in this@debugLiteral) {
            append(
                when (ch) {
                    '\n'     -> "\\n"
                    '\r'     -> "\\r"
                    '\t'     -> "\\t"
                    '\u0000' -> "\\u0000"
                    else -> if (ch.isISOControl()) "\\u%04X".format(ch.code) else ch
                }
            )
        }
    }
}
