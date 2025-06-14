package com.example.androidapprpg.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.androidapprpg.ui.activity.ActivityJoinGame
import com.example.androidapprpg.ui.activity.ActivityMyGames
import com.example.androidapprpg.ui.activity.ActivityNewGame
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

    //Realizo uma intent para uma Activity para criar um novo Backstack. Cada intent abre um novo fluxo de trabalho
    private fun setupClickListeners() {
        //binding para navegar para ActivityNewGame.
        binding.NewGame.setOnClickListener {
            val intent = Intent(requireContext(), ActivityNewGame::class.java)
            startActivity(intent)
        }

        //binding para navegar para Fragment My Games
        binding.MyGames.setOnClickListener {
            val intent = Intent(requireContext(), ActivityMyGames::class.java)
            startActivity(intent)
        }

        //binding para navegar para Fragment Join Game
        binding.JoinGame.setOnClickListener {
            val intent = Intent(requireContext(), ActivityJoinGame::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}