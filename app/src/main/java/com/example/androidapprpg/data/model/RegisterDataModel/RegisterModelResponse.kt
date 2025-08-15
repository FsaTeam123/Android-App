package com.example.androidapprpg.data.model.RegisterDataModel

import com.google.gson.annotations.SerializedName

data class RegisterModelResponse(

    @SerializedName("nome")
    val nome : String?,

    @SerializedName("email")
    val email : String?,

    @SerializedName("nickname")
    val nickname : String?,

    @SerializedName("senha")
    val senha : String?,

    @SerializedName(value = "idSexo", alternate = ["id_Sexo"])
    val idSexo : Int? = null,

    @SerializedName(value = "idPerfil", alternate = ["id_Perfil"])
    val idPerfil : Int? = null

)
