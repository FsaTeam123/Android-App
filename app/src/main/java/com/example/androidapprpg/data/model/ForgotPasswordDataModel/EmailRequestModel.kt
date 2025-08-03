package com.example.androidapprpg.data.model.ForgotPasswordDataModel

import com.google.gson.annotations.SerializedName

data class EmailRequestModel(

    @SerializedName("email")
    val email : String

)
