package com.example.androidapprpg.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.androidapprpg.databinding.ActivityNewGameBinding

class ActivityNewGame : AppCompatActivity() {

    private lateinit var binding : ActivityNewGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewGameBinding.inflate(layoutInflater)
        setContentView(binding.root)




    }



}