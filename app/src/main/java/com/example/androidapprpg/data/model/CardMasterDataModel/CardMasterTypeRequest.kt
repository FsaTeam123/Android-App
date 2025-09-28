package com.example.androidapprpg.data.model.CardMasterDataModel

import com.google.gson.annotations.SerializedName

data class CardMasterTypeRequest(

    @SerializedName("id")
    val id : Int?,

    @SerializedName("type")
    val type :  String?,

    )
