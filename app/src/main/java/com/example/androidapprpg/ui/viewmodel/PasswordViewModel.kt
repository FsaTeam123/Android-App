package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.repository.ForgotPasswordRepository
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordViewModel @Inject constructor(private val repository: ForgotPasswordRepository) : ViewModel() {

    // 1) Enviar e-mail
    private val _forgotPassword = MutableLiveData<Result<Unit>>()
    val forgotPasswordResult: LiveData<Result<Unit>> = _forgotPassword

    // 2) Verificar código recebido por e-mail
    private val _verifyCode = MutableLiveData<Result<Unit>>()
    val verifyCodeResult: LiveData<Result<Unit>> = _verifyCode

    // 3) Definir nova senha
    private val _setNewPassword = MutableLiveData<Result<Unit>>()
    val setNewPasswordResult: LiveData<Result<Unit>> = _setNewPassword

    /** 1) Solicita envio do e-mail de recuperação */
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPassword.value = Result.Loading
            try {
                val response = repository.requestForgotPassword(email)
                if (response.isSuccessful) {
                    _forgotPassword.value = Result.Success(Unit)
                } else {
                    _forgotPassword.value = Result.Error("Erro ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _forgotPassword.value = Result.Error("Falha na rede: ${e.message}")
            }
        }
    }

    /** 2) Verifica o código digitado pelo usuário */
    fun verifyCode(email: String, codigo: String) {
        viewModelScope.launch {
            _verifyCode.value = Result.Loading
            try {
                val response = repository.verifyCode(email, codigo)
                if (response.isSuccessful) {
                    _verifyCode.value = Result.Success(Unit)
                } else {
                    _verifyCode.value = Result.Error("Erro ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _verifyCode.value = Result.Error("Falha na rede: ${e.message}")
            }
        }
    }

    /**
     * 3) Define a nova senha.
     * Observação: o repositório atual não recebe token.
     * Mantive a assinatura (email, token, newPassword) para compatibilidade com a UI,
     * mas o 'token' é ignorado aqui.
     */
    fun setNewPassword(email: String, token: String, newPassword: String) {
        viewModelScope.launch {
            _setNewPassword.value = Result.Loading
            try {
                val response = repository.updatePassword(email, newPassword)
                if (response.isSuccessful) {
                    _setNewPassword.value = Result.Success(Unit)
                } else {
                    _setNewPassword.value = Result.Error("Erro ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                _setNewPassword.value = Result.Error("Falha na rede: ${e.message}")
            }
        }
    }

    // Resetando os estados do LiveData para evitar disparo indesejado
    fun resetPasswordState() {
        _forgotPassword.value = Result.StopViewModel
        _verifyCode.value = Result.StopViewModel
        _setNewPassword.value = Result.StopViewModel
    }
}
