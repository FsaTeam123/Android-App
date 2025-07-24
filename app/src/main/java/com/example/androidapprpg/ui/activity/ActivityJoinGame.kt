package com.example.androidapprpg.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidapprpg.databinding.ActivityJoinGamesBinding

class ActivityJoinGame : AppCompatActivity (){

    private lateinit var binding:ActivityJoinGamesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityJoinGamesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ativarFullscreen()
        setUpEnterGame()
        setUpExitButton()

    }

    private fun ativarFullscreen() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setUpExitButton() {
        binding.btnFechar.setOnClickListener {
            startActivity(Intent(this, ActivityMaster::class.java))
        }
    }

    private fun setUpEnterGame() {
        binding.btnEntrarJogo.setOnClickListener {
            val idGame = binding.inputIdJogo.text.toString()

            if(idGame.isEmpty()) {
                Toast.makeText(this, "Preencha o ID de Jogo", Toast.LENGTH_SHORT).show()
            } else {
                //verificar e validar no servidor
                startActivity(Intent(this, ActivityGame::class.java))
            }

        }
    }



}