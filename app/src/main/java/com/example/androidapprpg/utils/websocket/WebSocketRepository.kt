package com.example.androidapprpg.utils.websocket

import javax.inject.Inject

class WebSocketRepository @Inject constructor(private val service: WebSocketService) {
    fun connect(url: String, token: String, gameId: String) = service.connect(url, token, gameId)
    fun send(json: String) = service.send(json)
    fun close() = service.close()
}