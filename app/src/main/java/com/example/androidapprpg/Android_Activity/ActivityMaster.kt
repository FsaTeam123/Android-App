package com.example.androidapprpg.Android_Activity


import android.os.Bundle

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.ActivityMasterBinding


class ActivityMaster : AppCompatActivity() {

    private lateinit var binding: ActivityMasterBinding

    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityMasterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
        val navController = navHostFragment!!.findNavController()

        val popupMenu = PopupMenu(this, binding.bottomNavigation)
        popupMenu.inflate(R.menu.bottom_navigation_menu)
        binding.bottomNavigation.setupWithNavController(navController)



    }

}