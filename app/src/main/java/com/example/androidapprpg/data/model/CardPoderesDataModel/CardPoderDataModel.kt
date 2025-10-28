package com.example.androidapprpg.data.model.CardPoderesDataModel

import com.google.gson.annotations.SerializedName

data class CardPoderDataModel(

    @SerializedName("idPoder")
    val idPoder: Int?,

    @SerializedName("nome")
    val nome : String?,

    @SerializedName("descricao")
    val descricao : String?,

    @SerializedName("tipoPoder")
    val tipoPoder : TipoPoderDataModel?,

    @SerializedName("imagem")
    val imagem : String?,

    @SerializedName("imagemContentType")
    val imagemContentType : String?,

    @SerializedName("imagemFilename")
    val imagemFilename : String?
)
