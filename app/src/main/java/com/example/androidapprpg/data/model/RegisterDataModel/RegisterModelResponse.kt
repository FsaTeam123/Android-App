package com.example.androidapprpg.data.model.RegisterDataModel

import com.google.gson.annotations.SerializedName

data class RegisterModelResponse(

    @SerializedName("idUsuario")
    val idUsuario : Int?,

    @SerializedName("nome")
    val nome : String?,

    @SerializedName("email")
    val email : String?,

    @SerializedName("nickname")
    val nickname : String?,

    @SerializedName("status")
    val status : String?

)
