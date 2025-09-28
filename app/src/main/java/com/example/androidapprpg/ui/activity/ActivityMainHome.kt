package com.example.androidapprpg.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import com.example.androidapprpg.R
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityMainHome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_home)
        forceImmersive()
    }

    // Reaplica quando a activity volta a ter foco
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) forceImmersive()
    }

    private fun forceImmersive() {
        // desenha por trás das barras (sem reservar espaço)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // oculta status + navegação (imersivo total)
        val c = WindowInsetsControllerCompat(window, window.decorView)
        c.hide(WindowInsetsCompat.Type.systemBars())
        c.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // ocupa a área do notch
        if (Build.VERSION.SDK_INT >= 28) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        // teclado empurra o conteúdo
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )

        // fallback para APIs < 30
        if (Build.VERSION.SDK_INT < 30) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}