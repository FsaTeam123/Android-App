package com.example.androidapprpg.ui.activity



import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
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

    private lateinit var binding: ActivityMasterBinding //Inicialização tardia
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityMasterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        esconderSystemBars()
        ativarFullscreen()

        enableEdgeToEdge()


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        val navController = navHostFragment!!.findNavController()


        val popupMenu = android.widget.PopupMenu(this, null)
        popupMenu.inflate(R.menu.bottom_navigation_menu)
        binding.bottomNavigation.setupWithNavController(popupMenu.menu, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.newGameFragment,
                R.id.joinGameFragment,
                R.id.myGamesFragment,
                R.id.mapFragment,
                R.id.lettersFragment,
                R.id.settingsFragment -> {
                    expandirFragmentoFullScreen()
                }
                else -> {
                    restaurarLayoutNormal()
                }
            }
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            ativarFullscreen()
        }
    }

    private fun esconderSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun expandirFragmentoFullScreen() {
        binding.header.visibility = View.GONE
        binding.bottomNavigation.visibility = View.GONE

        val params = binding.fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        binding.fragmentContainerView.layoutParams = params
    }

    private fun restaurarLayoutNormal() {
        binding.header.visibility = View.VISIBLE
        binding.bottomNavigation.visibility = View.VISIBLE

        val params = binding.fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = binding.header.id
        params.bottomToTop = binding.bottomNavigation.id
        binding.fragmentContainerView.layoutParams = params
    }

    private fun ativarFullscreen() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}