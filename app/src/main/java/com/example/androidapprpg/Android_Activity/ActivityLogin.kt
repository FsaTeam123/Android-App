package com.example.androidapprpg.Android_Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.ActivityLoginBinding
import android.content.ContentValues.TAG


class ActivityLogin : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen() //instalar Splashscreen
        setContentView(R.layout.activity_login) //linka com o Layout Activity Login

    //linkando databinding para interagir com o Front
    binding = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(binding.root)

    //sharedPreferences
    val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val userId = sharedPreferences.getLong("USER_ID", -1)

    //LOG
    Log.d(TAG, "User ID iniciou LoginActivity: $userId ")

    if(userId != -1L) {
        //usuario já está logado redirecionar para ActivityMaster
        val intent = Intent(this, ActivityMaster::class.java)
        startActivity(intent)
        finish()
    } else {
        Log.d(TAG, "Nenhum usuario logado, permanecer na tela de login")
    }

    //Configurando Botão de Login
    binding.loginButton.setOnClickListener {
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            //realiza as verificações no Banco - AWS


            //Realiza o Intent para a próxima Activity
            val intent = Intent(this, ActivityMaster::class.java)
            startActivity(intent)
        }
    }


    //Configurando botão de Esquecer a Senha
    binding.forgotPassword.setOnClickListener {
        val intent = Intent(this, ActivityCadastro::class.java)
        startActivity(intent)
        }
    }
}
