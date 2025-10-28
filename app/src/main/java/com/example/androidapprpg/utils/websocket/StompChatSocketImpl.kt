package com.example.androidapprpg.utils.websocket

import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StompChatSocketImpl @Inject constructor(
    private val stomp: StompChatSocket,
    private val gson: Gson
) : ChatSocket {

    override fun send(chatId: String, msg: ChatMessage) {
        stomp.sendChat(chatId, msg)
    }

    override fun subscribe(chatId: String, onMessage: (String) -> Unit): () -> Unit {
        return stomp.subscribe(chatId, onMessage)
    }

    override fun connectIfNeeded() = stomp.connect()
    override fun disconnect() = stomp.disconnect()
}