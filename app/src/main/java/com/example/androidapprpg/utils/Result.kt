package com.example.androidapprpg.utils


//Representa o estado de Requisição
//<out T> --> Define que o tipo da minha classe é generica --> out = define que T só será usado como saida
// sealed class --> só pode conter subclasses, porem pode ser acessado de outras classes
//obejct Loading --> cria um singleton
//: Result<Nothing>() --> herda de result com o tipo nothing, mas não há dados a serem passados
//data class --> utilizado quando for necesario armazenar qualquer tipo de dado, por isso o generico T

sealed class Result <out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message : String) : Result<Nothing>()

}