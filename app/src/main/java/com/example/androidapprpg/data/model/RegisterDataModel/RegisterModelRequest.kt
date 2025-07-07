package com.example.androidapprpg.data.model.RegisterDataModel

import com.google.gson.annotations.SerializedName

data class RegisterModelRequest(
    @SerializedName("name")
    val name : String?,

    @SerializedName("email")
    val email : String?,

    @SerializedName("nickname")
    val nickname : String?,

    @SerializedName("senha")
    val senha : String?,

    )
