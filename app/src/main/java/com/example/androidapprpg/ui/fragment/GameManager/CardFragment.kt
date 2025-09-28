package com.example.androidapprpg.ui.fragment.GameManager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidapprpg.databinding.FragmentCardsManagementBinding


class CardFragment : Fragment() {

    private var _binding: FragmentCardsManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCardsManagementBinding.inflate(inflater, container, false)
        return binding.root
    }


}