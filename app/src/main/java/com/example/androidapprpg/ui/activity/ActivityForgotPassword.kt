package com.example.androidapprpg.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.androidapprpg.utils.Result
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidapprpg.databinding.ActivityForgotPasswordBinding
import com.example.androidapprpg.ui.viewmodel.PasswordViewModel
import dagger.hilt.android.AndroidEntryPoint


//Essa tela precisa enviar um email? ou precisa acessar um endereço web?
//Precsiamos que essa tela avance para uma novca tela, onde a nova tela recebe um código de verificação que foi enivado por email e o usuario consiga seguir com o logon
@AndroidEntryPoint
class ActivityForgotPassword : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: PasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ativarFullscreen()
        allButtons()
        observarResultado()
    }

    private fun ativarFullscreen() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun allButtons() {
        binding.backToLogin.setOnClickListener {
            startActivity(Intent(this, ActivityLogin::class.java))
        }

        binding.enviar.setOnClickListener {
            val email = binding.email.text.toString()
            if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                viewModel.forgotPassword(email)
            } else {
                Toast.makeText(this, "Insira um email válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observarResultado() {
        viewModel.forgotPasswordResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    Toast.makeText(this, "Enviando email...", Toast.LENGTH_SHORT).show()
                    // Você pode exibir um ProgressBar aqui
                }
                is Result.Success -> {
                    Toast.makeText(this, "Email enviado com sucesso", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, ActivityTokenVerification::class.java)
                    startActivity(intent)
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
