package com.example.androidapprpg.ui.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.model.RegisterDataModel.RegisterApiResponse
import com.example.androidapprpg.data.model.RegisterDataModel.RegisterModelRequest
import com.example.androidapprpg.data.model.RegisterDataModel.SexoDataModel.SexoDataModel
import com.example.androidapprpg.data.repository.RegisterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.example.androidapprpg.utils.Result
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: RegisterRepository
) : ViewModel() {

    private val _registerResult = MutableLiveData<Result<RegisterApiResponse>>()
    val registerResult: LiveData<Result<RegisterApiResponse>> = _registerResult

    private val _sexos = MutableLiveData<List<SexoDataModel>>()
    val sexos: LiveData<List<SexoDataModel>> = _sexos

    private val _comboError = MutableLiveData<String?>()
    val comboError: LiveData<String?> = _comboError

    fun loadCombos() {
        viewModelScope.launch {
            try {
                val sexos = repository.getSexos()
                _sexos.value = sexos

            } catch (e: Exception) {
                _comboError.value = "Falha ao carregar listas: ${e.localizedMessage}"
            }
        }
    }

    fun register(name: String, email: String, nickname: String, senha: String,idSexo: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _registerResult.postValue(Result.Loading)
            try {
                val request = RegisterModelRequest( nome = name, email = email, nickname = nickname, senha = senha, idSexo = idSexo, idPerfil = 1)
                val response = repository.register(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _registerResult.postValue(Result.Success(body))
                    } else {
                        _registerResult.postValue(Result.Error("Resposta vazia do servidor."))
                    }
                } else {
                    val raw = response.errorBody()?.string()
                    val msg = extractServerMessage(raw) ?: "Erro HTTP ${response.code()}"
                    _registerResult.postValue(Result.Error(msg))
                }
            } catch (e: Exception) {
                _registerResult.postValue(Result.Error("Erro de conexão: ${e.localizedMessage}"))
            }
        }
    }


    private fun extractServerMessage(raw: String?): String? {
        return try {
            if (raw.isNullOrBlank()) null
            else org.json.JSONObject(raw).optString("message").takeIf { it.isNotBlank() }
        } catch (_: Exception) { null }
    }
}


