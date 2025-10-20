package com.example.androidapprpg.data.model.NewGameDataModel

import com.google.gson.annotations.SerializedName

data class TemasDataModel(

    @SerializedName("idTema")
    val idTema : Int?,

    @SerializedName("nome")
    val nome : String?,

    @SerializedName("descricao")
    val descricao : String?,

    @SerializedName("ativo")
    val ativo : Int?


)
