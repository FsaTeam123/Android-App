package com.example.androidapprpg.data.model.CardPlayerDataModel

import com.google.gson.annotations.SerializedName

data class RaçasDataModel(

    @SerializedName("idRaca")
    val idRaca : Int? = null,

    @SerializedName("nome")
    val nome : String? = null,

    @SerializedName("descricao")
    val descricao : String?,

    @SerializedName("ativo")
    val ativo : Int?,

    @SerializedName("fotoBase64")
    val fotoBase64 : String?,

    @SerializedName("fotoMime")
    val fotoMime : String?,

    @SerializedName("fotoNome")
    val fotoNome : String?,

    @SerializedName("fotoTam")
    val fotoTam : Long?,

    @SerializedName("fotoAtualizadaEm")
    val fotoAtualizadaEm : String?,

    @SerializedName("habilidades")
    val habilidades : HabilidadesDataModel? = null

)
