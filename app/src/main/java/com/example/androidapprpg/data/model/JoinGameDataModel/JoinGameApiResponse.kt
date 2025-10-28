package com.example.androidapprpg.data.model.JoinGameDataModel

import com.example.androidapprpg.data.model.NewGameDataModel.EstiloCampanhaDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.GeracaoMundoDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.HistoriaDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.MasterDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.TemasDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.TipoJogoDataModel
import com.google.gson.annotations.SerializedName

data class JoinGameApiResponse(

    @SerializedName("idJogo")
    val idJogo: Long? = null,

    @SerializedName("master")
    val master: MasterDataModel? = null,

    @SerializedName("titulo")
    val titulo: String? = null,

    @SerializedName("qtdPessoas")
    val qtdPessoas: Int? = null,

    @SerializedName("isEspecificClass")
    val isEspecificClass: Int? = null, // parece ser 0/1 (boolzinho)

    @SerializedName("nivelInicial")
    val nivelInicial: Int? = null,

    @SerializedName("tipoJogo")
    val tipoJogo: TipoJogoDataModel? = null,

    @SerializedName("geracaoMundo")
    val geracaoMundo: GeracaoMundoDataModel? = null,

    @SerializedName("estiloCampanha")
    val estiloCampanha: EstiloCampanhaDataModel? = null,

    @SerializedName("historia")
    val historia: HistoriaDataModel? = null,

    @SerializedName("tema")
    val tema: TemasDataModel? = null,

    @SerializedName("senha")
    val senha: String? = null,

    @SerializedName("dataCriacao")
    val dataCriacao: String? = null,

    @SerializedName("ativo")
    val ativo: Int? = null,

    @SerializedName("playerAtivos")
    val playerAtivos: Int? = null
)
