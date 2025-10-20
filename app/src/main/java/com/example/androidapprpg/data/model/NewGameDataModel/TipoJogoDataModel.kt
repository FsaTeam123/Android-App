package com.example.androidapprpg.data.model.NewGameDataModel

import com.google.gson.annotations.SerializedName

data class TipoJogoDataModel(

    @SerializedName("idTipoJogo")
    val idTipoJogo: Int?,

    @SerializedName("nome")
    val nome: String?,

    @SerializedName("ativo")
    val ativo: Int?
)

