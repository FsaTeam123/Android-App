package com.example.androidapprpg.data.model.CardPlayerDataModel

import com.google.gson.annotations.SerializedName

data class DivindadeDataModel(

    @SerializedName("id")
    val id : Int?,

    @SerializedName("divindade")
    val divindade : String?

)
