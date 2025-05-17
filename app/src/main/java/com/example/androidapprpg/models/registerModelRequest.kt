package com.example.androidapprpg.models

import com.google.gson.annotations.SerializedName

data class registerModelRequest(
    @SerializedName("name")
    val name : String?,

    @SerializedName("email")
    val email : String?,

    @SerializedName("nickname")
    val nickname : String?,

    @SerializedName("senha")
    val senha : String?,

    @SerializedName("sexo")
    val idsexo : String?, //pode ser nulo

    @SerializedName("perfil")
    val idPerfil : String? //pode ser nulo

)
