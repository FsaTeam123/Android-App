package com.example.androidapprpg.data.model.ForgotPasswordDataModel

import com.google.gson.annotations.SerializedName

data class UpdatePasswordRequest(

    @SerializedName("senha")
    val senha : String?

)
