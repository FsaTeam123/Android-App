package com.example.androidapprpg.data.model.CardMagiasDataModel

import com.google.gson.annotations.SerializedName

data class CardCustoDataModel(

    @SerializedName("id")
    val id : Int?,

    @SerializedName("custo")
    val custo : String?

)
