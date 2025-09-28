package com.example.androidapprpg.data.model.CardMasterDataModel

import com.google.gson.annotations.SerializedName

data class CardMasterItemRequest(


    @SerializedName("id")
    val id : Int?,

    @SerializedName("item")
    val item : String?,

)
