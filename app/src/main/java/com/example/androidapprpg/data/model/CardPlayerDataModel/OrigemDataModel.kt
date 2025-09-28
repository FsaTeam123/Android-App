package com.example.androidapprpg.data.model.CardPlayerDataModel

import com.google.gson.annotations.SerializedName

data class OrigemDataModel(

    @SerializedName("id")
    val id : Int?,

    @SerializedName("origem")
    val origem : String?
)
