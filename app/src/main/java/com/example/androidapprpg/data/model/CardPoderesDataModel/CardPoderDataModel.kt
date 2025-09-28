package com.example.androidapprpg.data.model.CardPoderesDataModel

import com.google.gson.annotations.SerializedName

data class CardPoderDataModel(

    @SerializedName("id")
    val id: Int?,

    @SerializedName("tipoPoder")
    val tipoPoder : String?

)
