package com.example.androidapprpg.data.model.RegisterDataModel

import com.google.gson.annotations.SerializedName

data class RegisterApiResponse(

    @SerializedName("status")
    val status : Int?,

    @SerializedName("message")
    val message : String?,

    @SerializedName("data")
    val data : RegisterModelResponse?

)
