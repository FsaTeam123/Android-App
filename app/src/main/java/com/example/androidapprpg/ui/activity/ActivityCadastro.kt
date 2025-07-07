package com.example.androidapprpg.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidapprpg.databinding.ActivityCadastroBinding
import com.example.androidapprpg.ui.viewmodel.RegisterViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityCadastro : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private val viewModel : RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerButton()
        ativarFullscreen()
        observarCadastro()

    }

    private fun registerButton() {
        //Configurando botão de Cadastro
        binding.registerButton.setOnClickListener {
            val name = binding.nome.text.toString()
            val email = binding.email.text.toString()
            val nickname = binding.nickname.text.toString()
            val senha = binding.password.text.toString()
            val confirmarSenha = binding.checkPassword.text.toString()

            if(name.isNotEmpty() && email.isNotEmpty() && nickname.isNotEmpty() && senha.isNotEmpty() && confirmarSenha.isNotEmpty()) {
                if(senha != confirmarSenha) {
                    Toast.makeText(this, "As senhas precisam ser iguais", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.register(name, email, nickname, senha)
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        //Configurando Botão de Voltar para tela de Login
        binding.backToLogin.setOnClickListener {
            val intent = Intent(this, ActivityLogin ::class.java)
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

    private fun observarCadastro() {
        viewModel.registerResult.observe(this) { result ->
            when(result) {
                is Result.Loading -> {
                    //Exibir Progressbar
                }
                is Result.Success -> {
                    Toast.makeText(this, "Cadastro realizado com sucesso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ActivityLogin::class.java))
                    finish()
                }

                is Result.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()

                }

            }

        }

    }

}