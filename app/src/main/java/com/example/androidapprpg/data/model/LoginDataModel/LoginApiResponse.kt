package com.example.androidapprpg.data.model.LoginDataModel

import com.google.gson.annotations.SerializedName

data class LoginApiResponse(

    @SerializedName("status")
    val status : Int?,

    @SerializedName("message")
    val message : String?,

    @SerializedName("data")
    val data : UserResponseLogin

)
