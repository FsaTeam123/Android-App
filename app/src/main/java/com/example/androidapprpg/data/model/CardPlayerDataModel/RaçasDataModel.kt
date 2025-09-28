package com.example.androidapprpg.data.model.CardPlayerDataModel

import com.google.gson.annotations.SerializedName

data class RaçasDataModel(

    @SerializedName("id")
    val id : Int?,

    @SerializedName("raças")
    val raças : String?

)
