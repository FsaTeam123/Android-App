package com.example.androidapprpg.data.model.CardPoderesDataModel

import com.google.gson.annotations.SerializedName

data class TipoPoderDataModel(

    @SerializedName("idTipoPoder")
    val idTipoPoder : Int?,

    @SerializedName("nome")
    val nome : String?,

    @SerializedName("descricao")
    val descricao : String?

)
