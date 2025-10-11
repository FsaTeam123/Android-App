package com.example.androidapprpg.utils.websocket

import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class ChatSocketListener(
    private val httpClient: OkHttpClient,
    baseHttpUrl: String,
    private val gson: Gson = Gson()
) : WebSocketListener() {

    private var socket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _incoming = MutableSharedFlow<ChatMessage>(
        replay = 0, extraBufferCapacity = 32, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incoming: SharedFlow<ChatMessage> = _incoming

    private val wsUrl: String = buildWsUrl(baseHttpUrl)

    private fun buildWsUrl(httpUrl: String): String {
        // http(s) -> ws(s) e anexa "/ws"
        val httpToWs = if (httpUrl.startsWith("https://")) {
            "wss://" + httpUrl.removePrefix("https://")
        } else {
            "ws://" + httpUrl.removePrefix("http://")
        }
        return httpToWs.trimEnd('/') + "/ws"
    }

    /** Abre conexão e envia CONNECT + SUBSCRIBE para o chatId. */
    fun connect(chatId: String, headers: Headers = Headers.Builder().build()) {
        if (socket != null) return
        val req = Request.Builder().url(wsUrl).headers(headers).build()
        socket = httpClient.newWebSocket(req, this)
        // A sequência CONNECT/SUBSCRIBE é enviada em onOpen
        pendingSubscribeChatId = chatId
    }

    fun disconnect() {
        socket?.close(1000, "bye")
        socket = null
    }

    /** Envia uma mensagem para /app/chat.{chatId}.message */
    fun send(chatId: String, msg: ChatMessage) {
        val bodyJson = gson.toJson(msg)
        val frame = buildString {
            append("SEND\n")
            append("destination:/app/chat.").append(chatId).append(".message\n")
            append("content-type:application/json\n")
            append("\n")
            append(bodyJson)
            append("\u0000")
        }
        socket?.send(frame)
    }

    // -------- STOMP internals --------
    private var pendingSubscribeChatId: String? = null

    override fun onOpen(webSocket: WebSocket, response: Response) {
        // 1) CONNECT
        val connect = buildString {
            append("CONNECT\n")
            append("accept-version:1.2\n")
            append("host:mobile\n")
            append("\n")
            append("\u0000")
        }
        webSocket.send(connect)

        // 2) SUBSCRIBE ao tópico do chat
        pendingSubscribeChatId?.let { chatId ->
            val sub = buildString {
                append("SUBSCRIBE\n")
                append("id:sub-").append(chatId).append("\n")
                append("destination:/topic/chat.").append(chatId).append("\n")
                append("\n")
                append("\u0000")
            }
            webSocket.send(sub)
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // Ignora heartbeats e RECEIPT; extrai body entre "\n\n" e "\0"
        val split = text.indexOf("\n\n")
        if (split <= 0) return
        val body = text.substring(split + 2).trimEnd('\u0000')
        if (body.isBlank()) return
        runCatching { gson.fromJson(body, ChatMessage::class.java) }
            .onSuccess { msg ->
                scope.launch { _incoming.emit(msg) }
            }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        onMessage(webSocket, bytes.utf8())
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // Aqui você pode emitir um erro ou tentar reconectar
        t.printStackTrace()
    }

    companion object {
        /** OkHttp recomendado para WS: sem readTimeout e com ping. */
        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .pingInterval(20, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .build()
    }
}