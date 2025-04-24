package com.example.androidapprpg.Android_Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.ActivityMasterBinding

class ActivityMaster : AppCompatActivity() {

    private lateinit var binding:ActivityMasterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master)




    }
}