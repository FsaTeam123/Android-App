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
        // callback “global” — ok se só houver uma tela ativa por vez
        socket.onMessage(onMessage)
        return socket.subscribeTopicChat(chatId)
    }

    override fun connectIfNeeded() {
        // idempotente: só conecta se não estiver conectado
        if (!socket.isConnected()) {
            socket.connect()
        }
    }

    override fun disconnect() = socket.disconnect()
}
