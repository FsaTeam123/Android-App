package com.example.androidapprpg.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding:FragmentSettingsBinding? = null
    private val binding get()= _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false) // Infla o Layout usando ViewBinding
        return  binding.root //retorna a raiz da vieww inflada para ser exibida na tela
    }


    //implementar metódo de encerrar a conta
    private fun closeAccount() {

    }

    //evita vazamento de memória
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
