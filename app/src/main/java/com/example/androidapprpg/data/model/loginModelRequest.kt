package com.example.androidapprpg.data.model

import com.google.gson.annotations.SerializedName

data class loginModelRequest(
    @SerializedName("email")
    val email : String?,

    @SerializedName("senha")
    val senha : String?
)
