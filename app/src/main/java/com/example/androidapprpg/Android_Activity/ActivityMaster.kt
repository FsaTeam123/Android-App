package com.example.androidapprpg.Android_Activity


import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(
                    android.view.WindowInsets.Type.navigationBars() or
                            android.view.WindowInsets.Type.statusBars()
                )
                controller.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)

        if(navHostFragment != null){
            navController = navHostFragment!!.findNavController()
        }

        val popupMenu = PopupMenu(this, binding.bottomNavigation)
        popupMenu.inflate(R.menu.bottom_navigation_menu)
        binding.bottomNavigation.setupWithNavController(navController)


    }

}