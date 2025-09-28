package com.example.androidapprpg.data.model.CardMagiasDataModel

import com.google.gson.annotations.SerializedName

data class CardMagiaDataModel(

    @SerializedName("id")
    val id : Int?,

    @SerializedName("magias")
    val magias : String?,

)
