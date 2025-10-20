// app/src/main/java/com/example/androidapprpg/adapter/ChatSocketStompAdapter.kt
package com.example.androidapprpg.adapter

import com.example.androidapprpg.utils.websocket.ChatSocket
import com.example.androidapprpg.utils.websocket.StompChatSocket
import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatSocketStompAdapter @Inject constructor(
    private val stomp: StompChatSocket
) : ChatSocket {

    override fun connectIfNeeded() {
        stomp.connect()
    }

    override fun disconnect() {
        stomp.disconnect()
    }

    override fun subscribe(chatId: String, onMessage: (String) -> Unit): () -> Unit {
        stomp.onMessage(onMessage)
        return stomp.subscribeTopicChat(chatId)
    }

    override fun send(chatId: String, msg: ChatMessage) {
        // o StompChatSocket já serializa o objeto para JSON
        stomp.sendChat(chatId, msg)
    }
}
