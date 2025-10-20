package com.example.androidapprpg.data.model.NewGameDataModel

import com.google.gson.annotations.SerializedName

data class NewGamesDataModelRequest(

    @SerializedName("masterId")
    val masterId: Int,

    @SerializedName("titulo")
    val titulo: String,

    @SerializedName("qtdPessoas")
    val qtdPessoas: Int,

    @SerializedName("isEspecificClass")
    val isEspecificClass: Int, // 0/1

    @SerializedName("nivelInicial")
    val nivelInicial: Int,

    @SerializedName("tipoJogoId")
    val tipoJogoId: Int,

    @SerializedName("geracaoMundoId")
    val geracaoMundoId: Int,

    @SerializedName("estiloCampanhaId")
    val estiloCampanhaId: Int,

    @SerializedName("historiaId")
    val historiaId: Int,

    @SerializedName("temaId")
    val temaId: Int,

    @SerializedName("senha")
    val senha: String?,

    @SerializedName("ativo")
    val ativo: Int // 0/1

)

