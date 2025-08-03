package com.example.androidapprpg.data.model.NewGameDataModel

import com.google.gson.annotations.SerializedName

data class NewGamesDataModelRequest(

    @SerializedName("sistema")
    val sistema: String?,

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("idMaster")
    val idMaster: Int?,

    @SerializedName("titulo")
    val titulo: String?,

    @SerializedName("idHistoria")
    val idHistoria: Int?,

    @SerializedName("qtdPessoas")
    val qtdPessoas: Int?,

    @SerializedName("dificuldade") //isso aqui deveria ser de 0 a 20?
    val dificuldade: Int?,

    @SerializedName("idTipoJogo")
    val idTipoJogo: Int?,

    @SerializedName("idEstiloCampanha")
    val idEstiloCampanha: Int?,

    @SerializedName("idGeracaoMundo")
    val idGeracaoMundo: Int?,

    @SerializedName("idTema")
    val idTema: Int?,

    @SerializedName("senha")
    val senha: String?,

)

