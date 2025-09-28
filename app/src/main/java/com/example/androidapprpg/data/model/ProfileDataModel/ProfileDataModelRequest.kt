package com.example.androidapprpg.data.model.ProfileDataModel

import com.google.gson.annotations.SerializedName

data class ProfileDataModelRequest(

    @SerializedName("nome")
    val nome : String?,

    @SerializedName("email")
    val email : String?,

    @SerializedName("nickname")
    val nickname : String?,

    @SerializedName("idSexo")
    val idSexo : Int?,

    @SerializedName("senha")
    val senha : String?,

    )
