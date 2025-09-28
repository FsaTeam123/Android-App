package com.example.androidapprpg.ui.activity

import android.os.Bundle
import com.example.androidapprpg.R
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityMainForgotPassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_forgot_password)

    }

}