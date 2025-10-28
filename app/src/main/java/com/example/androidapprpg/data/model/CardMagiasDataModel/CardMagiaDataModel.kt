package com.example.androidapprpg.data.model.CardMagiasDataModel

import com.google.gson.annotations.SerializedName

data class CardMagiaDataModel(

    @SerializedName("idMagia") val idMagia : Int?,
    @SerializedName("nome") val nome : String?,
    @SerializedName("descricao") val descricao : String?,
    @SerializedName("duracao") val duracao : Int?,
    @SerializedName("alvoArea") val alvoArea : Int?,
    @SerializedName("custo") val custo : Int?,
    @SerializedName("circulo") val circulo : String?,
    @SerializedName("dano") val dano : String?,
    @SerializedName("ativo")  val ativo : Int?,
    @SerializedName("escolaMagia")val escolaMagia : EscolaMagiaDataModel?,
    @SerializedName("execucaoMagia") val execucaoMagia : ExecucaoMagiaDataModel?,
    @SerializedName("tipoMagia") val tipoMagia : TiposMagiaDataModel?,
    @SerializedName("resistencia") val resistencia : ResistenciaDataModel?,
    @SerializedName("imagem") val imagem : String?,
    @SerializedName("imagemContentType") val imagemContentType : String?,
    @SerializedName("imagemFilename") val imagemFilename : String?

)
