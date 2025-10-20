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
        object topics { fun chat(id: Any) = "/topic/chat.$id" }
        object app    { fun chatSend(id: Any) = "/app/chat.$id.message" }
    }

    companion object {
        private const val TAG = "STOMP"
        private fun t(sub: String) = "$TAG/$sub"
    }

    private var ws: WebSocket? = null
    private val isActive = AtomicBoolean(false)
    private val connecting = AtomicBoolean(false)
    private val sockJsOpened = AtomicBoolean(false)

    private val pendingSubs  = ConcurrentLinkedQueue<String>()
    private val pendingSends = ConcurrentLinkedQueue<Triple<String, String, Map<String, String>>>()

    var sockJsUseNullTerminator: Boolean = true

    private lateinit var wsUrl: String

    @Volatile private var connectHeaders: Map<String, String> = emptyMap()
    @Volatile private var messageCallback: ((String) -> Unit)? = null

    // -------------------- API --------------------

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
            openWebSocket(); return
        }
        warmUpSockJsCookieAsync { openWebSocket() }
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

    fun subscribe(destination: String, onMessage: (String) -> Unit): () -> Unit {
        messageCallback = onMessage
        return subscribe(destination)
    }

    fun subscribe(destination: String): () -> Unit {
        val id = "sub-${destination.trimStart('/')}"
        val cmd = buildSubscribe(destination, id)
        if (isActive.get()) sendRaw(cmd) else pendingSubs.add(cmd)
        return {
            runCatching { sendRaw(buildUnsubscribe(id)) }
                .onFailure { e -> Log.w(t("SUB"), "unsubscribe falhou: ${e.message}", e) }
        }
    }

    fun subscribeTopicChat(chatId: String): () -> Unit = subscribe(WS.topics.chat(chatId))

    fun sendChat(chatId: String, body: Any, headers: Map<String, String> = emptyMap()) {
        send(WS.app.chatSend(chatId), body, headers)
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
            Log.d(t("SEND"), "active=false -> enfileirando SEND $destination")
        }
    }

    // -------------------- Connection helpers --------------------

    private fun openWebSocket() {
        val req = Request.Builder()
            .url(wsUrl)
            // importante: origin aceito pelo backend (EnableWebSocketMessageBroker + SockJS)
            .header("Origin", "https://main.d3r5mqem6d9ler.amplifyapp.com")
            .build()
        ws = ok.newWebSocket(req, socketListener)
        Log.i("STOMP/CONNECT", "Connecting to $wsUrl (sockJs=$useSockJs)")
    }

    private fun warmUpSockJsCookieAsync(done: () -> Unit) {
        val infoUrl = base.toHttpUrl().newBuilder()
            .addPathSegment(sanitizeEndpoint(endpoint))
            .addPathSegment("info")
            .addQueryParameter("_", System.currentTimeMillis().toString())
            .build()

        ok.newCall(Request.Builder().url(infoUrl).get().build())
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.w("STOMP/CONNECT", "préflight /ws/info falhou: ${e.message}")
                    done()
                }
                override fun onResponse(call: Call, response: Response) {
                    Log.d("STOMP/CONNECT", "preflight /ws/info -> ${response.code}")
                    response.close()
                    done()
                }
            })
    }

    // -------------------- Listener --------------------

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(t("WS"), "onOpen code=${response.code} url=$wsUrl")
            if (!useSockJs) {
                // WS puro: CONNECT imediatamente
                sendRaw(buildSockJsStompFrame())
            } else {
                // SockJS: só envia CONNECT após receber "o"
                sockJsOpened.set(false)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (useSockJs) {
                when {
                    text == "o" -> {
                        Log.v(t("WS"), "SockJS OPEN (o)")
                        if (sockJsOpened.compareAndSet(false, true)) {
                            sendRaw(buildSockJsStompFrame()) // CONNECT agora
                        }
                        return
                    }
                    text == "h" -> { Log.v(t("WS"), "SockJS heartbeat (h)"); return }
                    text.startsWith("a[") -> {
                        parseSockJsArray(text).forEach { handleStompFrame(it) }
                        return
                    }
                    text.startsWith("c[") -> {
                        // c[code,"reason"]
                        isActive.set(false)
                        connecting.set(false)
                        sockJsOpened.set(false)
                        Log.w(t("WS"), "SockJS CLOSE recebido: $text")
                        return
                    }
                }
            }
            // WS puro ou payload STOMP direto
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
            response?.let { Log.e(t("WS"), "resp=${it.code} ${it.message}") }
            isActive.set(false)
            connecting.set(false)
            sockJsOpened.set(false)
        }
    }

    // -------------------- STOMP --------------------

    private fun handleStompFrame(raw: String) {
        raw.split('\u0000').forEach { part ->
            val frame = part.trim()
            if (frame.isEmpty() || !connecting.get()) return@forEach

            val command = frame.substringBefore('\n', frame)
            when {
                command.startsWith("CONNECTED") -> {
                    Log.i(t("STOMP"), "CONNECTED")
                    isActive.set(true)
                    // drena inscrições
                    var drained = 0
                    while (true) { val s = pendingSubs.poll() ?: break; drained++; sendRaw(s) }
                    Log.d(t("STOMP"), "SUBSCRIBEs drenados: $drained")
                    // drena envios
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
                    try { messageCallback?.invoke(body) } catch (e: Throwable) {
                        Log.e(t("MSG"), "callback error: ${e.message}", e)
                    }
                }
                command.startsWith("ERROR")   -> Log.e(t("STOMP"), "ERROR: ${preview(frame)}")
                command.startsWith("RECEIPT") -> Log.v(t("STOMP"), "RECEIPT: ${preview(frame)}")
                else -> Log.v(t("STOMP"), "FRAME: ${preview(frame)}")
            }
        }
    }

    // CONNECT minimalista (sem host e sem STOMP cmd)
    private fun buildSockJsStompFrame(): String {
        val stomp = buildString {
            append("CONNECT\n")
            append("accept-version:1.1,1.2\n")
            append("heart-beat:0,0\n")
            append("\n")
            append('\u0000')
        }
        return "a" + JSONArray().put(stomp).toString()
    }

    private fun buildSubscribe(destination: String, id: String): String = stompFrame(
        command = "SUBSCRIBE",
        headers = mapOf("id" to id, "destination" to destination, "ack" to "auto"),
        body = null
    )

    private fun buildUnsubscribe(id: String): String = stompFrame(
        command = "UNSUBSCRIBE",
        headers = mapOf("id" to id),
        body = null
    )

    private fun buildSend(
        destination: String,
        contentType: String,
        body: String,
        extraHeaders: Map<String, String> = emptyMap()
    ): String = stompFrame(
        command = "SEND",
        headers = mapOf("destination" to destination, "content-type" to contentType) + extraHeaders,
        body = body
    )

    private fun stompFrame(command: String, headers: Map<String, String>, body: String?): String {
        val sb = StringBuilder().apply {
            append(command).append('\n')
            headers.forEach { (k, v) -> append(k).append(':').append(v).append('\n') }
            append('\n')
            if (body != null) append(body)
            val needsNull = if (useSockJs) sockJsUseNullTerminator else true
            if (needsNull) append('\u0000')
        }
        val out = sb.toString()
        return if (useSockJs) wrapSockJs(out) else out
    }

    // -------------------- URL helpers --------------------

    private fun sanitizeEndpoint(raw: String): String = raw.trim().trim('/')

    private fun buildWsUrl(): String {
        val ep = sanitizeEndpoint(endpoint)
        val http = base.toHttpUrl().newBuilder().apply {
            addPathSegments(ep)
            if (useSockJs) {
                val serverId = "%03d".format((0..999).random())
                val sid = UUID.randomUUID().toString().replace("-", "")
                addPathSegment(serverId)
                addPathSegment(sid)
                addPathSegment("websocket")
            }
        }.build().toString()

        return when {
            http.startsWith("https://", true) -> http.replaceFirst("https://", "wss://")
            http.startsWith("http://",  true) -> http.replaceFirst("http://",  "ws://")
            else -> http
        }
    }

    // -------------------- SockJS helpers --------------------

    private fun wrapSockJs(payload: String): String {
        if (logBodies) Log.v(t("WS"), "SockJS wrap IN  (STOMP raw) literal=${payload.debugLiteral()}")
        val out = "a" + JSONArray().put(payload).toString()
        if (logBodies) Log.v(t("WS"), "SockJS wrap OUT (a[...]) literal=${out.debugLiteral()}")
        return out
    }

    private fun parseSockJsArray(text: String): List<String> {
        val json = text.substringAfter("a[", "").removeSuffix("]")
        if (json.isBlank()) return emptyList()
        return json.split("\",\"").map {
            it.trim('"')
                .replace("\\n", "\n")
                .replace("\\u0000", "\u0000")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
        }
    }

    private fun sendRaw(frame: String) {
        if (logBodies) {
            val lit = frame.debugLiteral()
            val bytes = frame.toByteArray(Charsets.UTF_8)
            val lastChar = frame.lastOrNull()
            Log.v(t("WIRE"), "OUT >>> literal=$lit")
            Log.v(t("WIRE"), "OUT >>> lenChars=${frame.length} lenBytes=${bytes.size} lastChar=$lastChar lastCode=${lastChar?.safeCode()}")
            Log.v(t("WIRE"), "OUT >>> hex=${bytes.joinToString(" ") { "%02X".format(it) }}")
        } else {
            Log.v(t("WIRE"), "OUT >>> ${preview(frame)}")
        }
        val ok = ws?.send(frame) ?: false
        if (!ok) Log.w(t("WIRE"), "sendRaw falhou (ws nulo ou fechado)")
    }

    // -------------------- util --------------------

    private fun redact(headers: Map<String, String>): Map<String, String> =
        headers.mapValues { (k, v) -> if (k.equals("authorization", true)) "***REDACTED***" else v }

    private fun preview(s: String, max: Int = 200): String =
        if (s.length <= max) s else s.substring(0, max) + "…(+${s.length - max})"

    private fun String.debugLiteral(): String = buildString {
        for (ch in this@debugLiteral) {
            append(
                when (ch) {
                    '\n' -> "\\n"
                    '\r' -> "\\r"
                    '\t' -> "\\t"
                    '\u0000' -> "\\u0000"
                    else -> if (ch.isISOControl()) "\\u%04X".format(ch.code) else ch
                }
            )
        }
    }

    private fun String.hexDumpUtf8(): String =
        this.toByteArray(Charsets.UTF_8).joinToString(" ") { "%02X".format(it) }

    private fun Char.safeCode(): Int = this.code
}
