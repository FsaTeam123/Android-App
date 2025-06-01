package com.example.androidapprpg.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidapprpg.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false) // Infla o Layout usando ViewBinding
        return binding.root //retorna a raiz da view inflada para ser exibida na tela
    }

    //evita vazamento de memória
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}