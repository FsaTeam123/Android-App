package com.example.androidapprpg.data.model

import com.google.gson.annotations.SerializedName

data class MyGamesDataModelRequest(

@SerializedName("titulo")
val titulo : String?,

@SerializedName("idMaster")
val id: Int?,

@SerializedName("dificuldade") //isso aqui deveria ser de 0 a 20?
val dificuldade: Int?,

@SerializedName("qtdPessoas")
val qtdPessoas: Int?,

)
