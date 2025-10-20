package com.example.androidapprpg.data.model.NewGameDataModel

import com.example.androidapprpg.data.model.PerfilDataModel.PerfilDataModel
import com.example.androidapprpg.data.model.RegisterDataModel.SexoDataModel.SexoDataModel
import com.google.gson.annotations.SerializedName

data class MasterDataModel(

    @SerializedName("idUsuario")
    val idUsuario: Int?,

    @SerializedName("nome")
    val nome: String?,

    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("sexo")
    val sexo: SexoDataModel?,

    @SerializedName("perfil")
    val perfil: PerfilDataModel?,

    @SerializedName("dtcCriacao")
    val dtcCriacao: String?,

    @SerializedName("ativo")
    val ativo: Int?,

    @SerializedName("online")
    val online: Int?




)
