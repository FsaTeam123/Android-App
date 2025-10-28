package com.example.androidapprpg.data.model.CardMagiasDataModel

import com.google.gson.annotations.SerializedName

data class ResistenciaDataModel(

    @SerializedName("idResistencia")
    val idResistencia : Int?,

    @SerializedName("nome")
    val nome : String?,

    @SerializedName("descricao")
    val descricao : String?,

    @SerializedName("ativo")
    val ativo : Int?

)
