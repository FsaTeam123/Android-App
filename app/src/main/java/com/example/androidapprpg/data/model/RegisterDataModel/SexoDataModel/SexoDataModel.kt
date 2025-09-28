package com.example.androidapprpg.data.model.RegisterDataModel.SexoDataModel

import com.google.gson.annotations.SerializedName

data class SexoDataModel(

    @SerializedName("idSexo")
    val idSexo : Int?,

    @SerializedName("nome")
    val nome : String?,

    @SerializedName("ativo")
    val ativo: Int? = null

)
