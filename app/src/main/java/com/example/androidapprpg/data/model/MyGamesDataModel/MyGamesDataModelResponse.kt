package com.example.androidapprpg.data.model.MyGamesDataModel

import android.os.Parcelable
import com.example.androidapprpg.data.model.NewGameDataModel.EstiloCampanhaDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.GeracaoMundoDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.HistoriaDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.MasterDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.TemasDataModel
import com.example.androidapprpg.data.model.NewGameDataModel.TipoJogoDataModel
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


data class MyGamesDataModelResponse(

    @SerializedName("idJogo") val idJogo: Long,
    @SerializedName("master") val master: MasterDataModel,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("qtdPessoas") val qtdPessoas: Int,
    @SerializedName("isEspecificClass") val isEspecificClass: Int,
    @SerializedName("nivelInicial") val nivelInicial: Int,
    @SerializedName("tipoJogo") val tipoJogo: TipoJogoDataModel,
    @SerializedName("geracaoMundo") val geracaoMundo: GeracaoMundoDataModel,
    @SerializedName("estiloCampanha") val estiloCampanha: EstiloCampanhaDataModel,
    @SerializedName("historia") val historia: HistoriaDataModel,
    @SerializedName("tema") val tema: TemasDataModel,
    @SerializedName("senha") val senha: String?,
    @SerializedName("dataCriacao") val dataCriacao: String, // pode virar Instant depois
    @SerializedName("ativo") val ativo: Int,
    @SerializedName("playerAtivos") val playerAtivos: Int
)
