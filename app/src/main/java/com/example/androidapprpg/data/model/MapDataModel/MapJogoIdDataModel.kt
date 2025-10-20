package com.example.androidapprpg.data.model.MapDataModel

import com.google.gson.annotations.SerializedName

data class MapJogoIdDataModel(

    @SerializedName("idMapa")
    val idMapa: Long,

    @SerializedName("idJogo")
    val idJogo: Long,

    @SerializedName("nome")
    val nome: String,

    @SerializedName("descricao")
    val descricao : String,

    @SerializedName("grid")
    val grid : Long,

    @SerializedName("ativo")
    val ativo : Int,

    @SerializedName("hasImage")
    val hasImage : Boolean

)
