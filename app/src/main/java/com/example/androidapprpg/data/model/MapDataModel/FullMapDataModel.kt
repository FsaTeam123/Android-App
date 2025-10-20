package com.example.androidapprpg.data.model.MapDataModel

import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelResponse
import com.google.gson.annotations.SerializedName

data class FullMapDataModel (

    @SerializedName("idMapa")
    val idMapa: Long,

    @SerializedName("jogo")
    val jogo: NewGamesDataModelResponse,

    @SerializedName("descricao")
    val descricao: String?,

    @SerializedName("nome")
    val nome: String,

    @SerializedName("grid")
    val grid: Int,

    @SerializedName("ativo")
    val ativo: Int,

    @SerializedName("hasImage")
    val hasImage: Boolean,

    @SerializedName("imagemContentType")
    val imagemContentType: String?

)