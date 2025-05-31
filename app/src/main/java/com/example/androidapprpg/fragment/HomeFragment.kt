package com.example.androidapprpg.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    //onCreateView ideal para inflar a view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    //onViewCreated ideal para listeners e lógica relacionada a UI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        //binding para navegar para Fragment New Game
        binding.NewGame.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_newGameFragment)
        }

        //binding para navegar para Fragment My Games
        binding.MyGames.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_myGamesFragment)
        }

        //binding para navegar para Fragment Join Game
        binding.JoinGame.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_JoinGameFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}