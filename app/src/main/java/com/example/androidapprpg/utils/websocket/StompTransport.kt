package com.example.androidapprpg.utils.websocket

import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StompTransport @Inject constructor(
    private val socket: StompChatSocket
) : ChatSocket {

    override fun send(chatId: String, msg: ChatMessage) {
        socket.sendChat(chatId, msg)
    }

    override fun subscribe(chatId: String, onMessage: (String) -> Unit): () -> Unit {
        // registra callback global e agenda subscribe
        return socket.subscribeChat(chatId, onMessage)
    }

    override fun connectIfNeeded() {
        socket.connectIfNeeded()
    }

    override fun disconnect() {
        socket.disconnect()
    }
}
