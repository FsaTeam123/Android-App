package com.example.androidapprpg.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(

    @SerializedName("token")
    val token: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("idUsuario")
    val idUsuario: Int,

    @SerializedName("nome")
    val nome: String?,

    @SerializedName("email")
    val email: String?

)
