package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.*
import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesDataModelResponse
import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesUpdateRequest
import com.example.androidapprpg.data.repository.MyGamesRepository
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log


private const val TAG_VM = "VM:MyGames"

@HiltViewModel
class MyGamesViewModel @Inject constructor(
    private val repository: MyGamesRepository
) : ViewModel() {

    private val _myGamesResult = MutableLiveData<Result<List<MyGamesDataModelResponse>>>()
    val myGamesResult: LiveData<Result<List<MyGamesDataModelResponse>>> = _myGamesResult

    private val _updateResult = MutableLiveData<Result<MyGamesDataModelResponse>>()
    val updateResult: LiveData<Result<MyGamesDataModelResponse>> = _updateResult

    private val _deleteResult = MutableLiveData<Result<Long>>()
    val deleteResult: LiveData<Result<Long>> = _deleteResult

    fun myGamesList(userId: Long) = viewModelScope.launch {
        Log.d(TAG_VM, "myGamesList(userId=$userId)")
        _myGamesResult.value = Result.Loading
        try {
            _myGamesResult.value = Result.Success(repository.getMyGames(userId))
        } catch (e: Exception) {
            Log.e(TAG_VM, "myGamesList ERROR", e)
            _myGamesResult.value = Result.Error(e.message ?: "Erro ao carregar jogos")
        }
    }

    fun updateGame(id: Long, req: MyGamesUpdateRequest) = viewModelScope.launch {
        Log.d(TAG_VM, "updateGame(id=$id, payload=$req)")
        _updateResult.value = Result.Loading
        try {
            _updateResult.value = Result.Success(repository.updateGame(id, req))
        } catch (e: Exception) {
            Log.e(TAG_VM, "updateGame ERROR id=$id", e)
            _updateResult.value = Result.Error(e.message ?: "Erro ao atualizar jogo")
        }
    }

    fun deleteGame(id: Long) = viewModelScope.launch {
        Log.d(TAG_VM, "deleteGame(id=$id)")
        _deleteResult.value = Result.Loading
        try {
            val ok = repository.deleteGame(id)
            if (ok) _deleteResult.value = Result.Success(id)
            else    _deleteResult.value = Result.Error("HTTP != 2xx")
        } catch (e: Exception) {
            Log.e(TAG_VM, "deleteGame ERROR id=$id", e)
            _deleteResult.value = Result.Error(e.message ?: "Erro ao excluir jogo")
        }
    }
}

