package com.example.androidapprpg.Android_Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.ActivityCadastroBinding

class ActivityCadastro : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Configurando botão de Cadastro
        binding.registerButton.setOnClickListener {
            val user = binding.nome.text.toString()
            val email = binding.email.text.toString()
            val nickname = binding.nickname.text.toString()
            val senha = binding.password.text.toString()

        }

        //Configurando Botão de Voltar para tela de Login
        binding.backToLogin.setOnClickListener {
            val intent = Intent(this, ActivityLogin ::class.java)
            startActivity(intent)
        }

    }
}