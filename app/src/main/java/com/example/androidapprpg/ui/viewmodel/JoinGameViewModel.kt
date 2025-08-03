package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.model.JoinGameDataModel.JoinGameApiResponse
import com.example.androidapprpg.data.repository.JoinGameRepository
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinGameViewModel @Inject constructor(private val repository: JoinGameRepository) : ViewModel() {

    private val _joinGameResult = MutableLiveData<Result<JoinGameApiResponse>>()
    val joinGameResult: LiveData<Result<JoinGameApiResponse>> = _joinGameResult

    fun joinGame(id: Int) {
        _joinGameResult.value = Result.Loading

        viewModelScope.launch {
            try {
                val response = repository.joinGame(id)
                if (response.isSuccessful && response.body() != null) {
                    _joinGameResult.value = Result.Success(response.body()!!)
                } else {
                    _joinGameResult.value = Result.Error("Erro: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _joinGameResult.value = Result.Error("Erro inesperado: ${e.localizedMessage}")
            }
        }
    }
}
