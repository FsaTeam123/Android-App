package com.example.androidapprpg.data.model.NotesDataModel

import com.google.gson.annotations.SerializedName

data class Note(
    @SerializedName("idAnotacao")
    val idAnotacao: Long,

    @SerializedName("jogoId")
    val jogoId: Long,

    @SerializedName("anotacao")
    val anotacao: String
)
