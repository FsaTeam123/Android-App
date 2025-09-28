package com.example.androidapprpg.data.model.ProfileDataModel

import com.example.androidapprpg.data.model.RegisterDataModel.SexoDataModel.SexoDataModel
import com.google.gson.annotations.SerializedName

data class ProfileDataModelResponse(

    @SerializedName("nome")
    val nome : String?,

    @SerializedName("email")
    val email : String?,

    @SerializedName("nickname")
    val nickname : String?,

    @SerializedName("sexo")
    val sexo: SexoDataModel? ,

    @SerializedName("foto")
    val fotoUrl : String?

)
