package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.repository.GameLobbyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.example.androidapprpg.utils.Result
import javax.inject.Inject

@HiltViewModel
class GameLobbyViewModel @Inject constructor(private val repository: GameLobbyRepository) :ViewModel() {

    private val _estadoSessao = MutableLiveData<Result<Unit>>()
    val estadoSessao : LiveData<Result<Unit>> = _estadoSessao

    fun iniciarSessao(idUsuario: Long, idJogo: Long) {
        viewModelScope.launch {
            _estadoSessao.value = Result.Loading
            _estadoSessao.value = repository.iniciarSessao(idUsuario, idJogo)
        }
    }

}