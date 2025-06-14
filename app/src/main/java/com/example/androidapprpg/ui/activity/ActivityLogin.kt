package com.example.androidapprpg.ui.activity


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.androidapprpg.databinding.ActivityLoginBinding
import android.os.Build
import androidx.activity.enableEdgeToEdge

class ActivityLogin : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen() //instalar Splashscreen
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

    //linkando databinding para interagir com o Front
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(
                    android.view.WindowInsets.Type.navigationBars() or
                            android.view.WindowInsets.Type.statusBars()
                )
                controller.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }

        //sharedPreferences
    //val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    //val userId = sharedPreferences.getLong("USER_ID", -1)

    //LOG
    //Log.d(TAG, "User ID iniciou LoginActivity: $userId ")

    /*if(userId != -1L) {
        //usuario já está logado redirecionar para ActivityMaster
        val intent = Intent(this, ActivityMaster::class.java)
        startActivity(intent)
        finish()
    } else {
        Log.d(TAG, "Nenhum usuario logado, permanecer na tela de login")
    }*/

        settingsButtons()

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


    }

}
