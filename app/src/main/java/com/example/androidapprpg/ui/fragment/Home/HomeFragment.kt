package com.example.androidapprpg.ui.fragment.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ativarFullscreen()
         _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root //Inflando o layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()

    }

    private fun setupClickListeners() {
        // Navega para o NewGameFragment
        binding.btnNewGame.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_newGame)
        }

        // Navega para o MyGamesFragment
        binding.btnMyGames.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_myGames)
        }

        // Navega para o JoinGameFragment
        binding.btnJoinGame.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_joinGame)
        }

        binding.profileImage.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }

        binding.settings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

    }

    private fun ativarFullscreen() {

            val insetsController = activity?.window?.let {
                WindowInsetsControllerCompat(it, it.decorView)
            }

            insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
