package com.example.androidapprpg.ui.fragment.GameManager

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidapprpg.R
import com.example.androidapprpg.adapter.MapAdapter
import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import com.example.androidapprpg.data.model.MapDataModel.MapSelectedMsg
import com.example.androidapprpg.databinding.FragmentMapBinding
import com.example.androidapprpg.ui.activity.ActivityGameMaster
import com.example.androidapprpg.ui.dialogs.MapFragmentNomeDialog
import com.example.androidapprpg.ui.viewmodel.MapViewModel
import com.example.androidapprpg.utils.Result
import com.example.androidapprpg.utils.websocket.StompChatSocket
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val vm: MapViewModel by  activityViewModels()
    private lateinit var adapter: MapAdapter

    @Inject lateinit var stomp: StompChatSocket

    private var idJogo: Long = -1L

    /** Abre a galeria; se houver mapa selecionado, troca a imagem;
     *  senão, cria um novo mapa já com essa imagem. */
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val selectedId = adapter.selectedId
        if (uri != null) {
            if (!selectedId.isNullOrBlank()) {
                vm.uploadImage(selectedId, uri)               // troca imagem do selecionado
            } else {
                vm.createMapWithImage(getString(R.string.novo_mapa), uri) // cria novo mapa
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.setFragmentResultListener(
            MapFragmentNomeDialog.RESULT_KEY, viewLifecycleOwner
        ) { _, bundle ->
            val id = bundle.getString(MapFragmentNomeDialog.RES_ID) ?: return@setFragmentResultListener
            val newName = bundle.getString(MapFragmentNomeDialog.RES_NAME) ?: return@setFragmentResultListener
            vm.rename(id, newName)
        }

        setupRecycler()
        setupSearch()
        setupClicks()
        collectState()
        collectEvents()

        // inicializa com o jogo atual
        idJogo = requireActivity().intent.getLongExtra(ActivityGameMaster.EXTRA_ID_JOGO, -1L)
        vm.attachGameAndRefresh(idJogo)

        // garante WS on
        // Ex.: se precisar auth: stomp.setAuth(mapOf("Authorization" to "Bearer $token"))
        stomp.connectIfNeeded()
    }

    private fun setupRecycler() {
        adapter = MapAdapter(
            onPreview = { showPreview(it) },
            onEdit    = { item -> novoNomeDialog(item) },
            onDelete  = { item -> vm.deleteMap(item.id) }, // lixeira remove imagem
            onChecked = { item, checked ->
                vm.setSelected(if (checked) item.id else null)
                // === NOVO: publica seleção para sincronizar com outros devices ===
                if (checked && idJogo > 0) {
                    runCatching {
                        stomp.sendMapaSelect(
                            idJogo = idJogo,
                            body = MapSelectedMsg(
                                mapaId = item.id.toLong(),
                                // transform opcional não é enviado aqui
                                senderId = null,
                                ts = java.time.Instant.now().toString()
                            )
                        )
                    }
                }
            }
        )
        binding.rvMaps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MapFragment.adapter
            val extraBottom = (12 * resources.displayMetrics.density).toInt()
            if (paddingBottom < extraBottom) {
                setPadding(paddingLeft, paddingTop, paddingRight, extraBottom)
            }
            clipToPadding = false
        }
    }

    private fun setupSearch() = with(binding.searchEditText) {
        doOnTextChanged { text, _, _, _ -> vm.setQuery(text?.toString().orEmpty()) }
    }

    private fun setupClicks() {
        binding.btnAddMap.setOnClickListener {
            // sempre abre a galeria; a ação é decidida no callback (acima)
            pickImage.launch("image/*")
        }
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { state ->
                    adapter.selectedId = state.selectedId
                    when (val r = state.result) {
                        is Result.Loading  -> adapter.submitList(emptyList())
                        is Result.Success  -> adapter.submitList(r.data)
                        is Result.Error    -> {
                            adapter.submitList(emptyList())
                            showCustomToast(r.message, requireContext())
                        }
                        is Result.StopViewModel -> Unit
                    }
                }
            }
        }
    }

    private fun collectEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.events.collect { ev ->
                    when (ev) {
                        is MapViewModel.UiEvent.ShowMessage ->
                            showCustomToast(getString(ev.resId), requireContext())
                    }
                }
            }
        }
    }

    private fun showCustomToast(message: String, context: Context) {
        val layout = LayoutInflater.from(context).inflate(R.layout.toast_layout, null)
        layout.findViewById<TextView>(R.id.toast_message).text = message
        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM, 0, 200)
        }.show()
    }

    private fun showPreview(item: MapDataModel) {
        MapPreviewDialog.newInstance(
            MapPreviewDialog.Args(
                title = item.name,
                imageUri = item.imageUri,
                imageUrl = item.imageUrl,
                heightFraction = 0.6f
            )
        ).show(childFragmentManager, "map_preview_dialog")
    }

    private fun novoNomeDialog(item: MapDataModel) {
        MapFragmentNomeDialog.newInstance(item.id, item.name)
            .show(childFragmentManager, "dialog_novo_nome")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
