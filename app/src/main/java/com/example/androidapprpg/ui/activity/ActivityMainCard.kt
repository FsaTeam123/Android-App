// com/example/androidapprpg/ui/activity/ActivityMainCard.kt
package com.example.androidapprpg.ui.activity

import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.androidapprpg.R
import com.example.androidapprpg.audio.MusicManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

@AndroidEntryPoint
class ActivityMainCard : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_JOGO = "extra.ID_JOGO"
        private const val STATE_ID_JOGO = "state.ID_JOGO"
    }

    @Inject lateinit var music: MusicManager

    /** Única fonte de verdade do ID do jogo nesta task */
    private var idJogo: Long = -1L

    /** Getter público para fragments */
    fun gameId(): Long = idJogo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_card)
        volumeControlStream = AudioManager.STREAM_MUSIC
        forceImmersive()

        // 1) Recupera de Intent (primeira vez) ou do estado salvo (process death/rotação)
        idJogo = intent.getLongExtra(EXTRA_ID_JOGO, -1L)
        if (idJogo <= 0L) {
            idJogo = savedInstanceState?.getLong(STATE_ID_JOGO, -1L) ?: -1L
        }

        if (idJogo <= 0L) {
            Toast.makeText(this, "Sessão inválida (idJogo ausente).", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2) Seta o grafo (opcional manter o argumento global; não é mais necessário para leitura)
        val navHost = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as? NavHostFragment
        if (savedInstanceState == null && navHost != null) {
            navHost.navController.setGraph(R.navigation.nav_cards, bundleOf("idJogo" to idJogo))
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            if (music.enabled.first()) music.play(MusicManager.Track.BATTLE, loop = true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(STATE_ID_JOGO, idJogo)
        super.onSaveInstanceState(outState)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) forceImmersive()
    }

    private fun forceImmersive() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val c = WindowInsetsControllerCompat(window, window.decorView)
        c.hide(WindowInsetsCompat.Type.systemBars())
        c.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= 28) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
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
