package com.example.androidapprpg.data.model.MyGamesDataModel

import com.google.gson.annotations.SerializedName

data class MyGamesUpdateRequest(

    @SerializedName("masterId") val masterId: Long,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("qtdPessoas") val qtdPessoas: Int,
    @SerializedName("isEspecificClass") val isEspecificClass: Int,
    @SerializedName("nivelInicial") val nivelInicial: Int,
    @SerializedName("tipoJogoId") val tipoJogoId: Long,
    @SerializedName("geracaoMundoId") val geracaoMundoId: Long,
    @SerializedName("estiloCampanhaId") val estiloCampanhaId: Long,
    @SerializedName("historiaId") val historiaId: Long,
    @SerializedName("temaId") val temaId: Long,
    @SerializedName("senha") val senha: String?,
    @SerializedName("ativo") val ativo: Int



)
