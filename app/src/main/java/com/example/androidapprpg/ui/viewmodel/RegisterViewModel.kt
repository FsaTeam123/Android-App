package com.example.androidapprpg.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.model.LoginDataModel.LoginModelRequest
import com.example.androidapprpg.data.model.RegisterDataModel.RegisterModelRequest
import com.example.androidapprpg.data.model.RegisterDataModel.RegisterModelResponse
import com.example.androidapprpg.data.repository.RegisterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.example.androidapprpg.utils.Result
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository : RegisterRepository) : ViewModel(){

    private val _registerResult = MutableLiveData<Result<RegisterModelResponse>>()
    val registerResult : LiveData<Result<RegisterModelResponse>> = _registerResult

    fun register(name: String?, email: String?, nickname: String?, senha: String?) {
        viewModelScope.launch {
            _registerResult.value = Result.Loading // Indica carregamento
            try {
                val request = RegisterModelRequest(name, email, nickname, senha)
                val response = repository.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val userData = response.body()!!.data
                    _registerResult.value = Result.Success(userData)
                } else {
                    val erro = response.errorBody()?.string()
                    _registerResult.value = Result.Error("Erro no cadastro: $erro")
                }
            } catch (e: Exception) {
                _registerResult.value = Result.Error("Erro de conexão: ${e.localizedMessage}")
            }
        }
    }
}

