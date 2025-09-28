package com.example.androidapprpg.ui.fragment.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.bumptech.glide.Glide
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.databinding.FragmentHomeBinding
import com.example.androidapprpg.ui.viewmodel.HomeViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    @Inject lateinit var session: SessionManager

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
        observarFoto()

        val userId = session.getUserIdOrNull()?.toInt()
        if (userId == null) {
            findNavController().navigate(R.id.login)
        } else {
            viewModel.getProfilePhoto(userId)
        }

    }

    private fun observarFoto() {
        viewModel.photoResult.observe(viewLifecycleOwner) { r ->
            when (r) {
                is Result.Loading -> binding.profileImage.alpha = 0.6f
                is Result.Success -> {
                    binding.profileImage.alpha = 1f
                    Glide.with(binding.profileImage)
                        .asBitmap()
                        .load(r.data)
                        .centerCrop()
                        .into(binding.profileImage)
                }
                is Result.Error -> {
                    binding.profileImage.alpha = 1f

                }
                is Result.StopViewModel -> Unit
            }
        }

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


    override fun onResume() {
        super.onResume()
        val id = session.getUserIdOrNull()?.toInt() ?: return
        viewModel.getProfilePhoto(id, forceRefresh = true) // ignora cache e baixa de novo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
