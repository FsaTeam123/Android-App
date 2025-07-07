package com.example.androidapprpg.data.model.LoginDataModel

import com.google.gson.annotations.SerializedName

data class UserResponseLogin(

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
