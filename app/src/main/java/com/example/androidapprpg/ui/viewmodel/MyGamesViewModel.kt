package com.example.androidapprpg.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesDataModelResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.repository.MyGamesRepository
import kotlinx.coroutines.launch
import com.example.androidapprpg.utils.Result
import javax.inject.Inject


@HiltViewModel
class MyGamesViewModel @Inject constructor(private val repository: MyGamesRepository) : ViewModel(){

    private val _myGamesResult = MutableLiveData<Result<List<MyGamesDataModelResponse>>>()
    val myGamesResult: LiveData<Result<List<MyGamesDataModelResponse>>> = _myGamesResult

    fun myGamesList() {
        viewModelScope.launch {
            _myGamesResult.value = Result.Loading
            try {
                val response = repository.myGamesList()
                if (response.isSuccessful && response.body() != null) {
                    _myGamesResult.value = Result.Success(response.body()!!)
                } else {
                    _myGamesResult.value = Result.Error("Erro ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _myGamesResult.value = Result.Error("Exceção: ${e.message}")
            }
        }
    }

}