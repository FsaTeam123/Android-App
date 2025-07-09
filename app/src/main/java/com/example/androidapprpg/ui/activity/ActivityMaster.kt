package com.example.androidapprpg.ui.activity



import android.os.Bundle
import androidx.activity.enableEdgeToEdge

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.ActivityMasterBinding


class ActivityMaster : AppCompatActivity() {

    private lateinit var binding: ActivityMasterBinding //Inicialização tardia
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityMasterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        val navController = navHostFragment!!.findNavController()


        val popupMenu = android.widget.PopupMenu(this, null)
        popupMenu.inflate(R.menu.bottom_navigation_menu)
        binding.bottomNavigation.setupWithNavController(popupMenu.menu, navController)

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