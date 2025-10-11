package com.example.androidapprpg.data.model.ChatDataModel

data class ChatMessage(
    val from: String? = null,
    val text: String,
    val ts: Long? = null
)
