package com.example.androidapprpg.data.model.CardMagiasDataModel

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialInfo

data class CardMagiasExecuçãoDataModel(

    @SerializedName("id")
    val id : Int?,

    @SerializedName("execução")
    val execução : String?

)
