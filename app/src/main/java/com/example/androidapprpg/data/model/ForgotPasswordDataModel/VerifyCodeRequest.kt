package com.example.androidapprpg.data.model.ForgotPasswordDataModel

import com.google.gson.annotations.SerializedName

data class VerifyCodeRequest(

    @SerializedName("email")
    val email : String?,

    @SerializedName("codigo")
    val codigo : String?


)
