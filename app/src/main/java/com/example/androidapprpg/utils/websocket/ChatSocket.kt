package com.example.androidapprpg.utils.websocket

import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage

interface ChatSocket {
    fun send(chatId: String, msg: ChatMessage)
    fun subscribe(chatId: String, onMessage: (String) -> Unit): () -> Unit
    fun connectIfNeeded()
    fun disconnect()
}
