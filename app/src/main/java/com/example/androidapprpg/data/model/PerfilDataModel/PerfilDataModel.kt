package com.example.androidapprpg.data.model.PerfilDataModel

import com.google.gson.annotations.SerializedName

data class PerfilDataModel(

    @SerializedName("idPerfil")
    val idPerfil: Int?,

    @SerializedName("nome")
    val nome: String?,

    @SerializedName("descricao")
    val descricao: String?,

    @SerializedName("ativo")
    val ativo: Int?

)
