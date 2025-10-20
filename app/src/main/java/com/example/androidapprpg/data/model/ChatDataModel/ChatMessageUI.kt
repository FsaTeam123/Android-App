package com.example.androidapprpg.data.model.ChatDataModel

data class ChatMessageUI(
    val senderId: Long,
    val senderNick: String,
    val text: String,
    val tsMillis: Long = System.currentTimeMillis(),
    val scope: String? = null
)