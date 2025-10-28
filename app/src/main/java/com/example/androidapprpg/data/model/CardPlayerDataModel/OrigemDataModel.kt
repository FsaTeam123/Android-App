package com.example.androidapprpg.data.model.CardPlayerDataModel

import com.google.gson.annotations.SerializedName

data class OrigemDataModel(

    @SerializedName("idOrigem")
    val idOrigem : Int? = null,

    @SerializedName("nome")
    val nome : String? = null,

    @SerializedName("descricao")
    val descricao : String? = null,

    @SerializedName("ativo")
    val ativo : Int? = null,

    @SerializedName("imagem")
    val imagem : String? = null,

    @SerializedName("imagemContentType")
    val imagemContentType : String? = null,

    @SerializedName("imagemFilename")
    val imagemFilename : String? = null
)
