package com.example.androidapprpg.Android_Activity


import android.nfc.Tag
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

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)

        if(navHostFragment != null){
            navController = navHostFragment!!.findNavController()
        }

        val popupMenu = PopupMenu(this, binding.bottomNavigation)
        popupMenu.inflate(R.menu.bottom_navigation_menu)
        binding.bottomNavigation.setupWithNavController(navController)


    }

}