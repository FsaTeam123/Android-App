package com.example.androidapprpg.data.model.CardPlayerDataModel

import com.google.gson.annotations.SerializedName

data class PericiasDataModel(

    @SerializedName("idPericia")
    val idPericia: Int? = null,

    @SerializedName("nome")
    val nome : String? = null,

    @SerializedName("descricao")
    val descricao : String? = null,

    @SerializedName("atributo")
    val atributo : AtributosDataModel? = null,

    @SerializedName("ativo")
    val ativo : Int? = null,

    @SerializedName("imagem")
    val imagem : String? = null,

    @SerializedName("imagemContentType")
    val imagemContentType : String? = null,

    @SerializedName("imagemFilename")
    val imagemFilename : String? = null,
)
