package com.example.androidapprpg.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapprpg.data.model.LoginDataModel.LoginModelRequest
import com.example.androidapprpg.data.model.LoginDataModel.UserResponseLogin
import com.example.androidapprpg.data.repository.AuthRepository
import kotlinx.coroutines.launch
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

//Projetado para manter os dados da UI durante mudanças de configuração --> e mantém a lógica de negócio
//Essa classe processa os dados da tela de login e separa a lógica da UI
//LiveData --> LiveData permite armazenar dados observaveis e notificar os observadores quando esses dados forem modificados
//loginResult --> A Activity/Fragment pode observar, mas não modificar -- TIPO PUBLICO

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<UserResponseLogin>>() //LiveData permite armazenar dados observaveis e notificar os observadores quando esses dados forem modificados
    val loginResult : LiveData<Result<UserResponseLogin>> = _loginResult //Variavel para Observar --> Acessar essa variavel diretamente da Activity/Fragment para que _loginResult não tenha contato direto com a ui, visto que declaramos ele como privado

    fun login(email: String, senha: String) { // inicia o processo de Login, chamada pela UI
        viewModelScope.launch { //Corrotina --> Executar código assincrono, sem travar interface, se não tivesse iria travar a tela
            _loginResult.value = Result.Loading // Atualiza o liveData para indicar que o login esta em andamento
            try {
                val response = repository.login(LoginModelRequest(email, senha))
                if (response.isSuccessful && response.body() != null) { // Caso a API retorne HTPP 200 e a chamada for bem sucedida,
                    val userData = response.body()!!.data
                    _loginResult.value = Result.Success(userData) //a ui será notificada para navegar para a próxima tela
                    Log.d("API_SUCCESS", "Token: ${userData.token}")
                } else {
                    _loginResult.value = Result.Error("Email ou senha incorretos") //Se a resposta falhar atualiza Result.Error
                    Log.d("API_Failure", "Código HTTP: ${response.code()}, Erro: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                    _loginResult.value = Result.Error("Erro de conexão: ${e.localizedMessage}") //Caso tenha qualquer exceção --> falha de rede
            }
        }

    }

}