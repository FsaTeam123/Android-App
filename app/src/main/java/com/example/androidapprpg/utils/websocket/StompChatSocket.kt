package com.example.androidapprpg.utils.websocket

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.json.JSONArray
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
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
            fun chat(id: Any) = "/topic/chat.$id"
            fun mapaSelected(idJogo: Any) = "/topic/mapa.$idJogo.selected"
        }
        object app {
            fun chatSend(id: Any) = "/app/chat.$id.message"
            fun mapaSelect(idJogo: Any) = "/app/mapa.$idJogo.select"
        }
    }

    companion object {
        private const val TAG = "STOMP"
        private fun t(sub: String) = "$TAG/$sub"
    }

    private var ws: WebSocket? = null

    private val isActive     = AtomicBoolean(false)
    private val connecting   = AtomicBoolean(false)
    private val sockJsOpened = AtomicBoolean(false)

    // pendências
    private data class PendingSub(val cmd: String, val subId: String)
    private val pendingSubs  = ConcurrentLinkedQueue<PendingSub>()
    private val pendingSends = ConcurrentLinkedQueue<Triple<String, String, Map<String, String>>>()

    // callbacks por subscription-id
    private val subCallbacks = ConcurrentHashMap<String, (String) -> Unit>()

    var sockJsUseNullTerminator: Boolean = true

    private lateinit var wsUrl: String
    @Volatile private var connectHeaders: Map<String, String> = emptyMap()

    // -------------------- API pública --------------------

    fun setAuth(headers: Map<String, String>) {
        connectHeaders = headers
        Log.d(t("AUTH"), "setAuth headers=${redact(headers)}")
    }

    fun connect() {
        if (!connecting.compareAndSet(false, true)) return
        wsUrl = buildWsUrl()
        if (!useSockJs) { openWebSocket(); return }
        warmUpSockJsCookieAsync { openWebSocket() }
    }

    fun connectIfNeeded() {
        if (!isActive.get() && !connecting.get()) connect()
    }

    fun isConnected(): Boolean = isActive.get()

    fun disconnect() {
        Log.i(t("CONNECT"), "disconnect()")
        isActive.set(false); connecting.set(false); sockJsOpened.set(false)
        ws?.close(1000, "bye"); ws = null
        subCallbacks.clear()
        pendingSubs.clear()
        pendingSends.clear()
    }

    // ===== Assinaturas específicas =====

    fun subscribeChat(id: String, onMessage: (String) -> Unit): () -> Unit =
        subscribeInternal(WS.topics.chat(id), onMessage)

    fun sendChat(chatId: String, body: Any, headers: Map<String, String> = emptyMap()) {
        send(WS.app.chatSend(chatId), body, headers)
    }

    fun subscribeMapaSelected(idJogo: Long, onMessage: (String) -> Unit): () -> Unit =
        subscribeInternal(WS.topics.mapaSelected(idJogo), onMessage)

    fun sendMapaSelect(idJogo: Long, body: Any, headers: Map<String, String> = emptyMap()) {
        send(WS.app.mapaSelect(idJogo), body, headers)
    }

    // ===== Genéricos =====

    private fun subscribeInternal(destination: String, onMessage: (String) -> Unit): () -> Unit {
        val subId = "sub-${destination.trimStart('/')}"
        val cmd = buildSubscribe(destination, subId)

        subCallbacks[subId] = onMessage

        if (isActive.get()) {
            sendRaw(cmd)
        } else {
            pendingSubs.add(PendingSub(cmd, subId))
            Log.d(t("SUB"), "SUB pendente $destination")
        }

        return {
            runCatching {
                sendRaw(buildUnsubscribe(subId))
                subCallbacks.remove(subId)
            }.onFailure { e ->
                Log.w(t("SUB"), "unsubscribe falhou: ${e.message}", e)
            }
        }
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
            Log.d(t("SEND"), "SEND pendente $destination")
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
            .addPathSegment(sanitizeEndpoint(endpoint))
            .addPathSegment("info")
            .addQueryParameter("_", System.currentTimeMillis().toString())
            .build()

        ok.newCall(Request.Builder().url(infoUrl).get().build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(t("CONNECT"), "préflight /ws/info falhou: ${e.message}"); done()
            }
            override fun onResponse(call: Call, response: Response) {
                Log.d(t("CONNECT"), "preflight /ws/info -> ${response.code}")
                response.close(); done()
            }
        })
    }

    // -------------------- WebSocketListener --------------------

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(t("WS"), "onOpen code=${response.code} url=$wsUrl")
            if (!useSockJs) {
                sendRaw(buildNativeConnectFrame())
            } else {
                sockJsOpened.set(false)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (useSockJs) {
                when {
                    text == "o" -> {
                        Log.v(t("WS"), "SockJS OPEN (o)")
                        if (sockJsOpened.compareAndSet(false, true)) {
                            val connectPayload = buildNativeConnectFrame()
                            sendRaw(wrapSockJsClient(connectPayload))
                        }
                        return
                    }
                    text == "h" -> { Log.v(t("WS"), "SockJS heartbeat (h)"); return }
                    text.startsWith("a[") -> { parseSockJsServerFrame(text).forEach { handleStompFrame(it) }; return }
                    text.startsWith("c[") -> {
                        isActive.set(false); connecting.set(false); sockJsOpened.set(false)
                        Log.w(t("WS"), "SockJS CLOSE: $text"); return
                    }
                }
            }
            handleStompFrame(text)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.w(t("WS"), "onClosed code=$code reason=$reason")
            isActive.set(false); connecting.set(false); sockJsOpened.set(false)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(t("WS"), "onFailure: ${t.message}", t)
            response?.let { Log.e(t("WS"), "resp=${it.code} ${it.message}") }
            isActive.set(false); connecting.set(false); sockJsOpened.set(false)
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
                    Log.i(t("STOMP"), "CONNECTED 🎉")
                    isActive.set(true)

                    var drainedSubs = 0
                    while (true) {
                        val ps = pendingSubs.poll() ?: break
                        drainedSubs++
                        sendRaw(ps.cmd)
                    }
                    Log.d(t("STOMP"), "SUBs drenados: $drainedSubs")

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
                    val headersEnd = frame.indexOf("\n\n")
                    val headerBlock = if (headersEnd > 0) frame.substring(0, headersEnd) else frame
                    val body = if (headersEnd > 0) frame.substring(headersEnd + 2) else ""

                    val subId = headerBlock
                        .lineSequence()
                        .firstOrNull { it.startsWith("subscription:") }
                        ?.substringAfter("subscription:")
                        ?.trim()

                    if (logBodies) Log.v(t("MSG"), "sub=$subId body=$body")

                    subId?.let { id ->
                        subCallbacks[id]?.invoke(body)
                            ?: Log.w(t("MSG"), "sem callback para subId=$id")
                    } ?: Log.w(t("MSG"), "sem header 'subscription' em MESSAGE")
                }

                command.startsWith("ERROR")   -> Log.e(t("STOMP"), "ERROR: ${preview(frame)}")
                command.startsWith("RECEIPT") -> Log.v(t("STOMP"), "RECEIPT: ${preview(frame)}")
                else                          -> Log.v(t("STOMP"), "FRAME: ${preview(frame)}")
            }
        }
    }

    // -------------------- STOMP frames builders --------------------

    private fun buildNativeConnectFrame(): String = buildString {
        append("CONNECT\n")
        append("accept-version:1.1,1.0\n")
        append("heart-beat:10000,10000\n")
        connectHeaders.forEach { (k, v) -> append(k).append(':').append(v).append('\n') }
        append("\n").append('\u0000')
    }

    private fun buildSubscribe(destination: String, id: String): String =
        stompFrame("SUBSCRIBE", mapOf("id" to id, "destination" to destination, "ack" to "auto"), null)

    private fun buildUnsubscribe(id: String): String =
        stompFrame("UNSUBSCRIBE", mapOf("id" to id), null)

    private fun buildSend(destination: String, contentType: String, body: String, extraHeaders: Map<String, String>) =
        stompFrame("SEND", mapOf("destination" to destination, "content-type" to contentType) + extraHeaders, body)

    private fun stompFrame(command: String, headers: Map<String, String>, body: String?): String {
        val sb = StringBuilder().apply {
            append(command).append('\n')
            headers.forEach { (k, v) -> append(k).append(':').append(v).append('\n') }
            append('\n')
            if (body != null) append(body)
            val needsNull = if (useSockJs) sockJsUseNullTerminator else true
            if (needsNull) append('\u0000')
        }
        val rawFrame = sb.toString()
        return if (useSockJs) wrapSockJsClient(rawFrame) else rawFrame
    }

    // -------------------- SockJS helpers --------------------

    private fun buildWsUrl(): String {
        val ep = sanitizeEndpoint(endpoint)
        val httpUrl = base.toHttpUrl().newBuilder().apply {
            addPathSegments(ep)
            stage?.let { st -> if (st.isNotBlank()) addPathSegment(st.trim('/')) }
            if (useSockJs) {
                val serverId = "%03d".format((0..999).random())
                val sid = UUID.randomUUID().toString().replace("-", "")
                addPathSegment(serverId)
                addPathSegment(sid)
                addPathSegment("websocket")
            }
        }.build().toString()

        return when {
            httpUrl.startsWith("https://", true) -> httpUrl.replaceFirst("https://", "wss://")
            httpUrl.startsWith("http://",  true) -> httpUrl.replaceFirst("http://",  "ws://")
            else -> httpUrl
        }
    }

    private fun sanitizeEndpoint(raw: String): String = raw.trim().trim('/')

    private fun wrapSockJsClient(payload: String): String {
        val out = JSONArray().put(payload).toString()
        if (logBodies) Log.v(t("WS"), "SockJS CLIENT OUT literal=${out.debugLiteral()}")
        return out
    }

    private fun parseSockJsServerFrame(text: String): List<String> {
        val inner = text.substringAfter("a[", "").removeSuffix("]")
        if (inner.isBlank()) return emptyList()
        return inner.split("\",\"").map {
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
        if (!ok) Log.w(t("WIRE"), "sendRaw falhou (ws nulo/fechado)")
    }

    // -------------------- utils --------------------

    private fun redact(headers: Map<String, String>): Map<String, String> =
        headers.mapValues { (k, v) -> if (k.equals("authorization", true)) "***REDACTED***" else v }

    private fun preview(s: String, max: Int = 200): String =
        if (s.length <= max) s else s.substring(0, max) + "…(+${s.length - max})"

    private fun String.debugLiteral(): String = buildString {
        for (ch in this@debugLiteral) append(
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
