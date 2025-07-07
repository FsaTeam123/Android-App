package com.example.androidapprpg.ui.activity


import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.androidapprpg.databinding.ActivityLoginBinding
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.utils.Result
import com.example.androidapprpg.ui.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityLogin : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen() //instalar Splashscreen
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(applicationContext)
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Usuario já está logado: ${sessionManager.getUserId()}")
            startActivity(Intent(this, ActivityMaster::class.java))
            finish()
            return
        }

        enableEdgeToEdge()

    //linkando databinding para interagir com o Front
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsButtons()
        ativarFullscreen()
        observarLogin()

    }

        private fun settingsButtons() {

            //Configurando Botão de Login
            binding.loginButton.setOnClickListener {
                val email = binding.email.text.toString()
                val senha = binding.password.text.toString()

                //preciso salvar o usuario no sharedPrefences da aplicação de modo privado.
                if (email.isNotEmpty() && senha.isNotEmpty()) {
                    viewModel.login(email, senha)
                    Log.d(TAG, "API access")
                } else {
                    Toast.makeText(this, "Preencha email e senha", Toast.LENGTH_SHORT).show()
                }
            }

            //Configurando botão de Esquecer a Senha
            binding.forgotPassword.setOnClickListener {
                val intent = Intent(this, ActivityForgotPassword::class.java)
                startActivity(intent)
                Log.d(TAG, "Entrando na seção esqueci minha senha?")
            }

            //Configurando botão de Cadastro
            binding.register.setOnClickListener {
                val intent = Intent(this, ActivityCadastro::class.java)
                startActivity(intent)
                Log.d(TAG,"Entrando na seção cadastro")
            }
        }

    private fun ativarFullscreen() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun observarLogin() {
        viewModel.loginResult.observe(this, Observer{ result ->
            when(result) {
                is Result.Loading -> {
                    //ProgressBar
                }

                is Result.Success -> {

                    val user = result.data

                    //Salva Dados
                    val sessionManager = SessionManager(applicationContext)
                    sessionManager.saveLogin(user.idUsuario.toLong(), user.token)


                    val intent = Intent(this, ActivityMaster::class.java)
                    startActivity(intent)
                    finish()
                }

                is Result.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }

            }


        })

    }

}






