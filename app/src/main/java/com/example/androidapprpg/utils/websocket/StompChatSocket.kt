package com.example.androidapprpg.utils.websocket

import android.util.Log
import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import okio.ByteString
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class StompChatSocket @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson,

    @Named("wsBase") private val baseHttps: String,

    // Stage do API Gateway (ex.: "ws" ou "prod"; use null se não tiver)
    @Named("wsStage") private val stage: String?,

    // Endpoint do backend (ex.: "ws")
    @Named("wsEndpoint") private val endpoint: String,

    // Se o backend for SockJS, vira /<endpoint>/websocket
    @Named("wsSockJs") private val useSockJs: Boolean = false
) : WebSocketListener(), ChatSocket {

    companion object { private const val TAG = "STOMP" }

    private fun buildWsUrl(): String {
        val scheme = if (baseHttps.startsWith("https://")) "wss://" else "ws://"
        val host = baseHttps.removePrefix("https://").removePrefix("http://").trimEnd('/')
        val stagePart = stage?.trim('/')?.let { "/$it" } ?: ""
        val endp = endpoint.trim('/')
        val tail = if (useSockJs) "/$endp/websocket" else "/$endp"
        return scheme + host + stagePart + if (endp.isNotEmpty()) tail else ""
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private val connected = AtomicBoolean(false)
    private var subscribeSent = false
    private var currentChatId: String? = null
    private var headers: Map<String, String> = emptyMap()

    private val _incoming = MutableSharedFlow<ChatMessage>(
        replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override fun messages(): Flow<ChatMessage> = _incoming.asSharedFlow()

    private val wsUrl: String by lazy { buildWsUrl() }

    override fun connect(chatId: String, headers: Map<String, String>) {
        currentChatId = chatId
        this.headers = headers
        if (webSocket != null) return

        val reqBuilder = Request.Builder()
            .url(wsUrl)
            // >>> AQUI ENTRA O SUBPROTOCOL STOMP <<<
            .addHeader("Sec-WebSocket-Protocol", "v12.stomp")

        // headers do handshake (Authorization, x-api-key etc.)
        headers.forEach { (k, v) -> reqBuilder.addHeader(k, v) }

        val request = reqBuilder.build()
        webSocket = client.newWebSocket(request, this)
        Log.d(TAG, "Connecting to $wsUrl (chatId=$chatId)")
    }

    override fun send(chatId: String, message: ChatMessage) {
        val ws = webSocket ?: return
        val frame = buildString {
            append("SEND\n")
            append("destination:/app/chat.").append(chatId).append(".message\n")
            append("content-type:application/json\n")
            append("\n")
            append(gson.toJson(message))
            append("\u0000")
        }
        ws.send(frame)
        Log.d(TAG, "SEND /app/chat.$chatId.message")
    }

    override fun disconnect() {
        connected.set(false)
        subscribeSent = false
        webSocket?.close(1000, "bye")
        webSocket = null
        Log.d(TAG, "Disconnected")
    }

    // ---- WebSocketListener ----
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "onOpen -> CONNECT")
        Log.d("STOMP", "onOpen code=${response.code} url=${response.request.url}")
        val connect = buildString {
            append("CONNECT\n")
            append("accept-version:1.1,1.2\n")
            append("heart-beat:10000,10000\n")
            append("host:mobile\n")
            append("\n")
            append("\u0000")
        }
        webSocket.send(connect)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        text.split('\u0000').forEach { frame ->
            if (frame.isBlank()) return@forEach
            val cmd = frame.substringBefore("\n")
            Log.d(TAG, "Frame: $cmd")
            when {
                cmd.startsWith("CONNECTED") -> { connected.set(true); sendSubscribeIfNeeded() }
                cmd.startsWith("MESSAGE") -> {
                    val body = frame.substringAfter("\n\n", "")
                    if (body.isNotBlank()) {
                        runCatching { gson.fromJson(body, ChatMessage::class.java) }
                            .onSuccess { raw ->
                                // fallback de ts para DiffUtil não engolir mensagens iguais
                                val msg = if (raw.ts == null) raw.copy(ts = System.currentTimeMillis()) else raw
                                scope.launch { _incoming.emit(msg) }
                            }
                            .onFailure { e -> Log.e(TAG, "JSON parse error", e) }
                    }
                }
                else -> Unit // RECEIPT/heartbeats/…
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) =
        onMessage(webSocket, bytes.utf8())

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.w(TAG, "Closed: $code / $reason")
        connected.set(false); subscribeSent = false; this.webSocket = null
        tryReconnect()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "Failure", t)
        connected.set(false); subscribeSent = false; this.webSocket = null
        tryReconnect()
    }

    private fun sendSubscribeIfNeeded() {
        val ws = webSocket ?: return
        val chatId = currentChatId ?: return
        if (!connected.get() || subscribeSent) return

        val sub = buildString {
            append("SUBSCRIBE\n")
            append("id:sub-").append(chatId).append("\n")
            append("destination:/topic/chat.").append(chatId).append("\n")
            append("\n")
            append("\u0000")
        }
        ws.send(sub)
        subscribeSent = true
        Log.d(TAG, "SUBSCRIBE /topic/chat.$chatId")
    }

    private fun tryReconnect() {
        val chatId = currentChatId ?: return
        scope.launch {
            delay(1500)
            Log.d(TAG, "Reconnecting…")
            connect(chatId, headers)
        }
    }
}
