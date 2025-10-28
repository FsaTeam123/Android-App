package com.example.androidapprpg.data.model.CardPlayerDataModel

import com.google.gson.annotations.SerializedName

data class RiquezaDataModel(

    @SerializedName("idRiqueza")
    val idRiqueza : Int? = null,

    @SerializedName("tibarOuro")
    val tibarOuro : Int? = null,

    @SerializedName("tibarPrata")
    val tibarPrata : Int? = null,

    @SerializedName("tibarCobre")
    val tibarCobre : Int? = null

)
