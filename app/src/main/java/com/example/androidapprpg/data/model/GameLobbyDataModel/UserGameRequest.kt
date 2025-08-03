package com.example.androidapprpg.data.model.GameLobbyDataModel

import com.google.gson.annotations.SerializedName

data class UserGameRequest(

    @SerializedName("id")
    val id : Long = 0L,

    @SerializedName("idUsuario")
    val idUsuario : Long?,

    @SerializedName("idJogo")
    val idJogo: Long?

)
