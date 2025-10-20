package com.example.androidapprpg.data.model.MapDataModel

import com.google.gson.annotations.SerializedName

data class createMapRequest(
    @SerializedName("jogo")
    val jogo: JogoRef,

    @SerializedName("nome")
    val nome: String,

    @SerializedName("descricao")
    val descricao: String? = null,

    @SerializedName("grid")
    val grid: Int? = null,

    @SerializedName("ativo")
    val ativo: Int = 1,

    @SerializedName("imagemContentType")
    val imagemContentType: String? = null
)

