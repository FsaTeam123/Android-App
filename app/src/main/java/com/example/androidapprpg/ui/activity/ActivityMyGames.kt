package com.example.androidapprpg.ui.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidapprpg.databinding.ActivityMyGamesBinding
import java.util.Locale

class ActivityMyGames :AppCompatActivity() {

    private lateinit var binding : ActivityMyGamesBinding
    private var searchText: String = ""
    //private lateinit var adapter: MyGameAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMyGamesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ativarFullscreen()
        setupExitButton()
        setupSearchListener()
        updateRecyclerView()
    }

    private fun ativarFullscreen() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setupExitButton() {
        binding.btnFechar.setOnClickListener {
            val intent = Intent(this, ActivityMaster::class.java)
            startActivity(intent)
            Log.d(TAG, "Retornando para Activity Master")

        }
    }

    private fun setupSearchListener() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Não faz nada
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Não faz nada
            }

            override fun afterTextChanged(p0: Editable?) {
                searchText = p0?.toString()?.lowercase(Locale.getDefault()) ?: ""
                updateRecyclerView()
            }
        })
    }

    private fun updateRecyclerView() {
        /*if (list.isEmpty()) {
            return
        }

        val filteredList = list.filter {
            it.name.lowercase(Locale.getDefault()).contains(searchText) ||
                    it.symbol.lowercase(Locale.getDefault()).contains(searchText)
        }

        adapter.updateData(filteredList)*/
    }

}