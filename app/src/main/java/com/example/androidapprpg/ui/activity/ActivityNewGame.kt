package com.example.androidapprpg.ui.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidapprpg.databinding.ActivityNewGameBinding

class ActivityNewGame : AppCompatActivity() {

    private lateinit var binding: ActivityNewGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove as barras do sistema (status e navegação)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityNewGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ativarFullscreen()
    }

    private fun ativarFullscreen() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}
