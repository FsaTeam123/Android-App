package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.model.ForgotPasswordDataModel.EmailRequestModel
import com.example.androidapprpg.data.remote.services.ForgotPasswordService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.example.androidapprpg.utils.Result
import javax.inject.Inject

@HiltViewModel
class PasswordViewModel @Inject constructor(private val forgotPasswordService: ForgotPasswordService) : ViewModel() {

    private val _forgotPassword = MutableLiveData<Result<Unit>>()
    val forgotPasswordResult: LiveData<Result<Unit>> = _forgotPassword

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPassword.value = Result.Loading

            try {
                val response = forgotPasswordService.forgotPassword(EmailRequestModel(email))
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
}

