package com.example.androidapprpg.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.ActivityGameManagerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityGameMaster : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_JOGO   = "extra.ID_JOGO"
        const val EXTRA_TITULO    = "extra.TITULO"
        const val EXTRA_MASTER_ID = "extra.MASTER_ID"
        const val EXTRA_ATIVO     = "extra.ATIVO"
        private const val TAG = "ActivityGameMaster"

        // chave para sobreviver a rotação/process death
        private const val STATE_ID_JOGO = "state.ID_JOGO"
    }

    private lateinit var binding: ActivityGameManagerBinding
    private var navController: NavController? = null

    /** Única fonte de verdade do ID dentro desta Activity */
    private var idJogo: Long = -1L
    fun gameId(): Long = idJogo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGameManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RECUPERA EXTRAS
        val idFromIntent = intent.getLongExtra(EXTRA_ID_JOGO, -1L)
        val idFromState  = savedInstanceState?.getLong(STATE_ID_JOGO, -1L) ?: -1L
        idJogo = if (idFromIntent > 0L) idFromIntent else idFromState

        val titulo   = intent.getStringExtra(EXTRA_TITULO) ?: ""
        val masterId = intent.getIntExtra(EXTRA_MASTER_ID, -1)
        val ativo    = intent.getIntExtra(EXTRA_ATIVO, 1)

        Log.d(TAG, "onCreate -> idJogo=$idJogo, titulo=$titulo, masterId=$masterId, ativo=$ativo")

        if (idJogo <= 0L) {
            finish() // sem id não há sessão válida
            return
        }

        // NAV / BOTTOM BAR
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        navController = navHostFragment!!.findNavController()

        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.bottom_navigation_menu_game_manager)
        binding.bottomNavigation.setupWithNavController(popupMenu.menu, navController!!)

        navController!!.addOnDestinationChangedListener { _, dest, _ ->
            val isChat = dest.id == R.id.ChatFragment
            val isDice = dest.id == R.id.dicePlayFragment
            binding.bottomNavigation.isVisible = !isChat && !isDice
            enterImmersive()
        }

        applyBottomBarInsetsForCutout()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(STATE_ID_JOGO, idJogo)
        super.onSaveInstanceState(outState)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersive()
    }

    private fun enterImmersive() {
        val ic = WindowInsetsControllerCompat(window, window.decorView)
        ic.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ic.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun applyBottomBarInsetsForCutout() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { v, insets ->
            val cut = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(bottom = cut.bottom)
            insets
        }
    }
}
