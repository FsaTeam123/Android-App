package com.example.androidapprpg.data.model.RegisterDataModel

import com.google.gson.annotations.SerializedName

data class RegisterModelRequest(
    @SerializedName("nome")
    val nome : String?,

    @SerializedName("email")
    val email : String?,

    @SerializedName("nickname")
    val nickname : String?,

    @SerializedName("senha")
    val senha : String?,

    @SerializedName("idSexo")
    val idSexo : Int?,

    @SerializedName("idPerfil")
    val idPerfil : Int?

)
