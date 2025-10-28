package com.example.androidapprpg.data.model.CardPlayerDataModel

import com.google.gson.annotations.SerializedName

data class DivindadeDataModel(

    @SerializedName("idDivindade")
    val idDivindade : Int? = null,

    @SerializedName("nome")
    val nome : String? = null,

    @SerializedName("descricao")
    val descricao : String? = null,

    @SerializedName("ativo")
    val ativo : Int? = null,

    @SerializedName(value = "imagemBase64", alternate = ["imagem"])
    val imagemBase64: String? = null,

    @SerializedName("imagemContentType")
    val imagemContentType : String? = null,

    @SerializedName("imagemFilename")
    val imagemFilename : String? = null

)
