package com.example.androidapprpg.data.model.NewGameDataModel

import com.google.gson.annotations.SerializedName

data class NewGamesDataModelResponse (

    @SerializedName("idJogo")
    val idJogo: Int?,

    @SerializedName("master")
    val master: MasterDataModel?,

    @SerializedName("titulo")
    val titulo: String?,

    @SerializedName("qtdPessoas")
    val qtdPessoas: Int?,

    @SerializedName("isEspecificClass")
    val isEspecificClass: Int?, // 0/1

    @SerializedName("nivelInicial")
    val nivelInicial: Int?,

    @SerializedName("tipoJogo")
    val tipoJogo: TipoJogoDataModel?,

    @SerializedName("geracaoMundo")
    val geracaoMundo: GeracaoMundoDataModel?,

    @SerializedName("estiloCampanha")
    val estiloCampanha: EstiloCampanhaDataModel?,

    @SerializedName("historia")
    val historia: HistoriaDataModel?,

    @SerializedName("tema")
    val tema: TemasDataModel?,

    @SerializedName("senha")
    val senha: String?,

    @SerializedName("dataCriacao")
    val dataCriacao: String?,

    @SerializedName("ativo")
    val ativo: Int? // 0/1

)
