package com.example.androidapprpg.ui.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.androidapprpg.R
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidapprpg.data.repository.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ActivityMainLogin : AppCompatActivity() {

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {

        // Instala SplashScreen e permite controlar a duração
        val splashScreen = installSplashScreen()

        // Variável para controle
        var keepSplashVisible = true

        // Duração fixa da Splash (2 segundos)
        window.decorView.postDelayed({
            keepSplashVisible = false
        }, 2000)

        // Aplica a condição à Splash
        splashScreen.setKeepOnScreenCondition {
            keepSplashVisible
        }

        super.onCreate(savedInstanceState)

        ativarFullscreen()

        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Usuario já está logado: ${sessionManager.getUserIdOrNull()}")
            startActivity(Intent(this, ActivityMainHome::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main_login)
    }

    private fun ativarFullscreen() {

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

}