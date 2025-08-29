package com.example.androidapprpg.data.model.LoginDataModel

import com.google.gson.annotations.SerializedName

data class LoginModelRequest(

    @SerializedName("ativo")
    val ativo : Int?,

    @SerializedName("email")
    val email : String?,

    @SerializedName("senha")
    val senha : String?
)
