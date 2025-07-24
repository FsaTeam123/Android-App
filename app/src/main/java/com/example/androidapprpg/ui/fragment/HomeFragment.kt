package com.example.androidapprpg.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentHomeBinding
import com.example.androidapprpg.ui.activity.ActivityNewGame

class
HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Navega para o NewGameFragment
        binding.NewGame.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_newGame)
        }

        // Navega para o MyGamesFragment
        binding.MyGames.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_myGames)
        }

        // Navega para o JoinGameFragment
        binding.JoinGame.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_joinGame)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
