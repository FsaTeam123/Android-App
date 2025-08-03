package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewPasswordViewModel : ViewModel(){

    private val _senhaValida = MutableLiveData<Boolean>()
    val senhaValida: LiveData<Boolean> = _senhaValida

    private val _senhaRequisitos = MutableLiveData<Map<String, Boolean>>()
    val senhaRequisitos: LiveData<Map<String, Boolean>> = _senhaRequisitos

    fun validarSenha(senha: String) {
        val requisitos = mapOf(
            "min" to (senha.length >= 8),
            "maiuscula" to senha.any { it.isUpperCase() },
            "numero" to senha.any { it.isDigit() },
            "especial" to senha.any { "!@#\$%^&*()_+=-{}[]|:;\"'<>,.?/~`".contains(it) }
        )

        _senhaRequisitos.value = requisitos
        _senhaValida.value = requisitos.values.all { it }
    }

    fun trocarSenha(nova: String, confirmacao: String): Boolean {
        return nova == confirmacao && senhaValida.value == true
    }

}