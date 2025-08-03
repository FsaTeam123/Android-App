package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelRequest
import com.example.androidapprpg.data.model.NewGameDataModel.NewGamesDataModelResponse
import com.example.androidapprpg.data.repository.NewGamesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NewGameViewModel @Inject constructor(private val repository: NewGamesRepository) : ViewModel() {

    private val _newGameResult = MutableLiveData<Result<NewGamesDataModelResponse>>()
    val newGamesResult : LiveData<Result<NewGamesDataModelResponse>> = _newGameResult

    fun criarJogo(request: NewGamesDataModelRequest) {
        viewModelScope.launch {
            try {
                val response = repository.criarJogo(request)
                if (response.isSuccessful && response.body() != null) {
                    _newGameResult.value = Result.success(response.body()!!)
                } else {
                    _newGameResult.value = Result.failure(Exception("Erro: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                _newGameResult.value = Result.failure(e)
            }
        }
    }

}