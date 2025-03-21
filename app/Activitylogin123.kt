package com.example.baymax_rpg


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.baymax_rpg.databinding.ActivityLoginBinding

class Activitylogin : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}
