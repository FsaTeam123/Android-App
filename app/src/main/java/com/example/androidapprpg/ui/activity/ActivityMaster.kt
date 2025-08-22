package com.example.androidapprpg.ui.activity



import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.ActivityMasterBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ActivityMaster : AppCompatActivity() {

    private lateinit var binding: ActivityMasterBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMasterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ativarFullscreen()

        // NavHost
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        navController = navHostFragment!!.findNavController()

        // Ajuste de insets para não “comer” o header sob a status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.header) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, sysBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        // Se quiser esconder o header em telas específicas, mantenha a lógica
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // telas full (sem header)
                R.id.newGameFragment,
                R.id.joinGameFragment,
                R.id.myGamesFragment,
                R.id.mapFragment,
                R.id.lettersFragment,
                R.id.settingsFragment -> expandirFragmentoFullScreen()

                else -> restaurarLayoutNormal()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) ativarFullscreen()
    }

    private fun expandirFragmentoFullScreen() {
        binding.header.visibility = View.GONE
        val params = binding.fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        binding.fragmentContainerView.layoutParams = params
    }

    private fun restaurarLayoutNormal() {
        binding.header.visibility = View.VISIBLE
        val params = binding.fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = binding.header.id
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID // <-- sem bottom bar
        binding.fragmentContainerView.layoutParams = params
    }

    private fun ativarFullscreen() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun settingButtons() {
        binding.settings


    }




}
