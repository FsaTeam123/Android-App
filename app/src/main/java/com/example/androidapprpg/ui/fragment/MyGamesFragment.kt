package com.example.androidapprpg.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.adapter.MyGamesAdapter
import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesDataModelResponse
import com.example.androidapprpg.databinding.FragmentMyGamesBinding
import com.example.androidapprpg.ui.viewmodel.MyGamesViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MyGamesFragment : Fragment() {

    private var _binding: FragmentMyGamesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyGamesViewModel by viewModels()

    private lateinit var adapter: MyGamesAdapter
    private var listaCompleta: List<MyGamesDataModelResponse> = listOf()
    private var searchText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyGamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ativarFullscreen()
        setupExitButton()
        setupSearchListener()
        setupRecyclerView()
        observarJogos()

        // Chama a API
        viewModel.myGamesList()
    }

    private fun ativarFullscreen() {
        val controller = requireActivity().window.decorView
        val insetsController = WindowInsetsControllerCompat(requireActivity().window, controller)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setupExitButton() {
        binding.btnFechar.setOnClickListener {
            findNavController().popBackStack()
            Log.d("MyGamesFragment", "Retornando para fragmento anterior")
        }
    }

    private fun setupSearchListener() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchText = s?.toString()?.lowercase(Locale.getDefault()) ?: ""
                updateRecyclerView()
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = MyGamesAdapter(emptyList()) { jogo ->
            // Aqui futuramente você irá navegar para a tela de detalhes do jogo
            // val action = MyGamesFragmentDirections.actionMyGamesFragmentToGameDetailFragment(jogo)
            // findNavController().navigate(action)

            // Por enquanto, apenas imprime no log para confirmar clique
            Log.d("MyGamesFragment", "Jogo clicado: ${jogo.titulo}")
        }
        binding.recyclerViewMyGames.adapter = adapter
    }

    private fun updateRecyclerView() {
        val filtered = listaCompleta.filter {
            it.titulo?.lowercase(Locale.getDefault())!!.contains(searchText)
        }
        adapter.updateData(filtered)
    }

    private fun observarJogos() {
        viewModel.myGamesResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    // TODO: Mostrar progresso se quiser
                }
                is Result.Success -> {
                    listaCompleta = result.data
                    updateRecyclerView()
                }
                is Result.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
