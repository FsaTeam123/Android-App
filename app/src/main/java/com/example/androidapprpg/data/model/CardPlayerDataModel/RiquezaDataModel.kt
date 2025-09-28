package com.example.androidapprpg.data.model.CardPlayerDataModel

import com.google.gson.annotations.SerializedName

data class RiquezaDataModel(

    @SerializedName("id")
    val id : Int?,

    @SerializedName("riqueza")
    val riqueza : String?

)
