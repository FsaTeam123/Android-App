package com.example.androidapprpg.data.model.NewGameDataModel

import com.google.gson.annotations.SerializedName

data class EstiloCampanhaDataModel(

    @SerializedName("idEstiloCampanha")
    val idEstiloCampanha : Int?,

    @SerializedName("nome")
    val nome : String?,

    @SerializedName("descricao")
    val descricao : String?,

    @SerializedName("ativo")
    val ativo : Int?

)
