package com.example.androidapprpg.data.model.MyGamesDataModel

import com.example.androidapprpg.data.model.NewGameDataModel.MasterDataModel
import com.google.gson.annotations.SerializedName

data class GameDataModel(

    @SerializedName("idJogo") val idJogo: Long,
    @SerializedName("titulo") val titulo: String?,
    @SerializedName("qtdPessoas") val qtdPessoas: Int?,
    @SerializedName("nivelInicial") val nivelInicial: Int?,
    @SerializedName("playerAtivos") val playerAtivos: Int?,
    @SerializedName("master") val master: MasterDataModel?


)
