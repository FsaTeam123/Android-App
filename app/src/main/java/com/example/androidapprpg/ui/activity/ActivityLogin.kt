package com.example.androidapprpg.ui.activity


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.androidapprpg.databinding.ActivityLoginBinding
import android.os.Build
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import com.example.androidapprpg.utils.Result
import com.example.androidapprpg.ui.viewmodel.LoginViewModel

class ActivityLogin : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen() //instalar Splashscreen
        super.onCreate(savedInstanceState)
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
                val password = binding.password.text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    val intent = Intent(this, ActivityMaster::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            //Configurando botão de Esquecer a Senha
            binding.forgotPassword.setOnClickListener {
                val intent = Intent(this, ActivityForgotPassword::class.java)
                startActivity(intent)
            }

            //Configurando botão de Cadastro
            binding.register.setOnClickListener {
                val intent = Intent(this, ActivityCadastro::class.java)
                startActivity(intent)
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
                    val intent = Intent(this, ActivityMaster::class.java)
                    startActivity(intent)
                    finish()
                }

                is Result.Error -> {
                    android.widget.Toast.makeText(this, result.message, android.widget.Toast.LENGTH_SHORT).show()
                }

            }


        })



    }



}






