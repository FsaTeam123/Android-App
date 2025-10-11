package com.example.androidapprpg.utils.websocket

import com.example.androidapprpg.data.model.ChatDataModel.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatSocket {
    fun connect(chatId: String, headers: Map<String, String> = emptyMap())
    fun messages(): Flow<ChatMessage>
    fun send(chatId: String, message: ChatMessage)
    fun disconnect()
}
