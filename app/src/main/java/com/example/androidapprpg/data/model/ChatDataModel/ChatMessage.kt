package com.example.androidapprpg.data.model.ChatDataModel

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    @SerializedName("senderId")
    val senderId: Long?,

    @SerializedName("senderNick")
    val senderNick: String?,

    @SerializedName("text")
    val text: String?,

    @SerializedName("ts")
    val tsMillis: Long = System.currentTimeMillis(),


    @SerializedName("scope")
    val scope: String?
)
