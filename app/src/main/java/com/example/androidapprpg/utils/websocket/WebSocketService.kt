package com.example.androidapprpg.utils.websocket

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketService @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS) // mantém conexão viva
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null

    fun connect(url: String, token: String, gameId: String): Flow<String> = callbackFlow {
        val req = Request.Builder()
            .url("$url?gameId=$gameId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                webSocket = ws
                // opcional: handshake inicial
                ws.send("""{"type":"join","gameId":"$gameId"}""")
            }
            override fun onMessage(ws: WebSocket, text: String) {
                trySend(text)
            }
            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(code, reason)
                close() // encerra o flow
            }
            override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) {
                close(t) // deixa o ViewModel decidir reconectar
            }
        }
        val ws = client.newWebSocket(req, listener)

        awaitClose {
            ws.cancel() // encerra ao cancelar o flow
            webSocket = null
        }
    }

    fun send(json: String): Boolean = webSocket?.send(json) == true
    fun close(code: Int = 1000, reason: String = "bye") { webSocket?.close(code, reason) }
}
