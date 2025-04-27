package com.example.androidapprpg.models

import com.google.gson.annotations.SerializedName

data class loginModelRequest(
    @SerializedName("email")
    val email : String,

    @SerializedName("password")
    val password : String
)
