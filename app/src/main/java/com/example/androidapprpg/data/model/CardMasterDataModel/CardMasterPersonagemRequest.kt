package com.example.androidapprpg.data.model.CardMasterDataModel

import com.google.gson.annotations.SerializedName

data class CardMasterPersonagemRequest(

    @SerializedName("id")
    val id : Int?,

    @SerializedName("personagem")
    val personagem : String?,

)
