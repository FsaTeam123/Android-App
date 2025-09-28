package com.example.androidapprpg.data.model.CardPoderesDataModel

import com.google.gson.annotations.SerializedName

data class TipoDePoderDataModel(

    @SerializedName("id")
    val id : Int?,

    @SerializedName("tipoDePoder")
    val tipoDePoder : String?

)
