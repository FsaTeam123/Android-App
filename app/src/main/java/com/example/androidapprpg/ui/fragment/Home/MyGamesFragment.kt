package com.example.androidapprpg.ui.fragment.Home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidapprpg.R
import com.example.androidapprpg.adapter.MyGamesAdapter
import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesDataModelResponse
import com.example.androidapprpg.data.model.MyGamesDataModel.MyGamesUpdateRequest
import com.example.androidapprpg.data.repository.SessionManager
import com.example.androidapprpg.databinding.FragmentMyGamesBinding
import com.example.androidapprpg.ui.activity.ActivityGameMaster
import com.example.androidapprpg.ui.dialog.EditGameDialogFragment
import com.example.androidapprpg.ui.viewmodel.MyGamesViewModel
import com.example.androidapprpg.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import android.app.AlertDialog

@AndroidEntryPoint
class MyGamesFragment : Fragment() {

    companion object {
        private const val TAG_UI = "UI:MyGames"
    }

    private var _binding: FragmentMyGamesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyGamesViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager

    private lateinit var adapter: MyGamesAdapter
    private var listaCompleta: List<MyGamesDataModelResponse> = emptyList()
    private var searchText: String = ""

    // ============================ LIFECYCLE ============================

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG_UI, "onCreateView()")
        _binding = FragmentMyGamesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG_UI, "onViewCreated()")
        super.onViewCreated(view, savedInstanceState)

        ativarFullscreen()
        binding.btnFechar.setOnClickListener {
            Log.d(TAG_UI, "btnFechar clicked -> popBackStack")
            findNavController().popBackStack()
        }

        setupRecyclerView()
        setupSearch()
        observarVM()

        val userId = sessionManager.getUserIdOrNull()?.toLong()
        Log.d(TAG_UI, "session userId=$userId")
        if (userId != null) {
            viewModel.myGamesList(userId)
        } else {
            toast("Sessão inválida. Faça login.")
        }
    }

    override fun onDestroyView() {
        Log.d(TAG_UI, "onDestroyView()")
        super.onDestroyView()
        _binding = null
    }

    // ============================ UI SETUP ============================

    private fun ativarFullscreen() {
        Log.d(TAG_UI, "ativarFullscreen()")
        val controller = requireActivity().window.decorView
        val insetsController = WindowInsetsControllerCompat(requireActivity().window, controller)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setupRecyclerView() {
        Log.d(TAG_UI, "setupRecyclerView()")
        adapter = MyGamesAdapter(
            emptyList(),
            onEnterClick = { jogo ->
                Log.d(TAG_UI, "onEnterClick id=${jogo.idJogo}")

                val idJogo   = jogo.idJogo
                val titulo   = jogo.titulo
                val senha    = jogo.senha // pode ser null/vazia
                val masterId = (jogo.master.idUsuario ?: -1).toLong()
                val ativo    = jogo.ativo

                // se o jogo tiver senha -> pede senha primeiro
                if (!senha.isNullOrBlank()) {
                    Log.d(TAG_UI, "Jogo $idJogo tem senha, pedindo antes de entrar")
                    pedirSenhaEAbrirJogo(
                        idJogo       = idJogo,
                        tituloJogo   = titulo,
                        senhaCorreta = senha,
                        masterId     = masterId,
                        ativo        = ativo
                    )
                } else {
                    Log.d(TAG_UI, "Jogo $idJogo sem senha, entrando direto")
                    abrirGameMaster(
                        idJogo     = idJogo,
                        tituloJogo = titulo,
                        masterId   = masterId,
                        ativo      = ativo
                    )
                }
            },
            onEditClick = { jogo ->
                Log.d(TAG_UI, "onEditClick id=${jogo.idJogo}")
                val tipoId     = (jogo.tipoJogo.idTipoJogo ?: 0).toLong()
                val geracaoId  = (jogo.geracaoMundo.idGeracaoMundo ?: 0).toLong()
                val estiloId   = (jogo.estiloCampanha.idEstiloCampanha ?: 0).toLong()
                val historiaId = (jogo.historia.idHistoria ?: 0).toLong()
                val temaId     = (jogo.tema.idTema ?: 0).toLong()
                val masterId   = jogo.master.idUsuario?.toLong() ?: 0L

                EditGameDialogFragment.newInstance(
                    idJogo   = jogo.idJogo,
                    titulo   = jogo.titulo,
                    qtd      = jogo.qtdPessoas,
                    nivel    = jogo.nivelInicial,
                    senha    = jogo.senha,
                    ativo    = jogo.ativo,
                    masterId = masterId,
                    isEspecificClass = jogo.isEspecificClass,
                    tipoId    = tipoId,
                    geracaoId = geracaoId,
                    estiloId  = estiloId,
                    historiaId= historiaId,
                    temaId    = temaId
                ).show(parentFragmentManager, "edit_game")
            },
            onDeleteClick = { jogo ->
                Log.d(TAG_UI, "onDeleteClick id=${jogo.idJogo}")
                viewModel.deleteGame(jogo.idJogo)
            }
        )

        binding.recyclerViewMyGames.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@MyGamesFragment.adapter
        }

        parentFragmentManager.setFragmentResultListener(
            EditGameDialogFragment.REQ_KEY, viewLifecycleOwner
        ) { _, b ->
            val payload = MyGamesUpdateRequest(
                masterId         = b.getLong(EditGameDialogFragment.ARG_MASTER_ID),
                titulo           = b.getString(EditGameDialogFragment.RES_TITULO).orEmpty(),
                qtdPessoas       = b.getInt(EditGameDialogFragment.RES_QTD),
                isEspecificClass = b.getInt(EditGameDialogFragment.ARG_ESPECIFIC),
                nivelInicial     = b.getInt(EditGameDialogFragment.RES_NIVEL),
                tipoJogoId       = b.getLong(EditGameDialogFragment.ARG_TIPO_ID),
                geracaoMundoId   = b.getLong(EditGameDialogFragment.ARG_GERACAO_ID),
                estiloCampanhaId = b.getLong(EditGameDialogFragment.ARG_ESTILO_ID),
                historiaId       = b.getLong(EditGameDialogFragment.ARG_HISTORIA_ID),
                temaId           = b.getLong(EditGameDialogFragment.ARG_TEMA_ID),
                senha            = b.getString(EditGameDialogFragment.RES_SENHA),
                ativo            = b.getInt(EditGameDialogFragment.RES_ATIVO)
            )
            val idJogo = b.getLong(EditGameDialogFragment.RES_IDJOGO)
            Log.d(TAG_UI, "Dialog result -> updateGame(id=$idJogo, payload=$payload)")
            viewModel.updateGame(idJogo, payload)
        }
    }

    private fun setupSearch() {
        Log.d(TAG_UI, "setupSearch()")
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchText = s?.toString()?.lowercase(Locale.getDefault()) ?: ""
                Log.d(TAG_UI, "search changed -> \"$searchText\"")
                updateRecycler()
            }
        })
    }

    // ============================ OBSERVERS ============================

    private fun observarVM() {
        Log.d(TAG_UI, "observarVM() - attach observers")

        viewModel.myGamesResult.observe(viewLifecycleOwner) {
            when (it) {
                is Result.Loading -> {
                    Log.d(TAG_UI, "myGamesResult=Loading")
                }
                is Result.Success -> {
                    Log.d(TAG_UI, "myGamesResult=Success size=${it.data.size}")
                    listaCompleta = it.data
                    updateRecycler()
                }
                is Result.Error -> {
                    Log.d(TAG_UI, "myGamesResult=Error msg=${it.message}")
                    toast(it.message)
                }
                is Result.StopViewModel -> {
                    Log.d(TAG_UI, "myGamesResult=StopViewModel")
                }
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) {
            when (it) {
                is Result.Loading -> {
                    Log.d(TAG_UI, "updateResult=Loading")
                }
                is Result.Success -> {
                    val up = it.data
                    Log.d(TAG_UI, "updateResult=Success id=${up.idJogo} titulo=${up.titulo}")
                    listaCompleta = listaCompleta.map { g ->
                        if (g.idJogo == up.idJogo) up else g
                    }
                    updateRecycler()
                    toast("Jogo atualizado!")
                }
                is Result.Error -> {
                    Log.d(TAG_UI, "updateResult=Error msg=${it.message}")
                    toast(it.message)
                }
                is Result.StopViewModel -> {
                    Log.d(TAG_UI, "updateResult=StopViewModel")
                }
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) {
            when (it) {
                is Result.Loading -> {
                    Log.d(TAG_UI, "deleteResult=Loading")
                }
                is Result.Success -> {
                    val removedId = it.data
                    Log.d(TAG_UI, "deleteResult=Success removedId=$removedId")
                    listaCompleta = listaCompleta.filter { g -> g.idJogo != removedId }
                    updateRecycler()
                    toast("Jogo excluído!")
                }
                is Result.Error -> {
                    Log.d(TAG_UI, "deleteResult=Error msg=${it.message}")
                    toast(it.message)
                }
                is Result.StopViewModel -> {
                    Log.d(TAG_UI, "deleteResult=StopViewModel")
                }
            }
        }
    }

    // ============================ HELPERS ============================

    private fun updateRecycler() {
        val filtered =
            if (searchText.isBlank()) listaCompleta
            else listaCompleta.filter {
                (it.titulo ?: "").lowercase(Locale.getDefault()).contains(searchText)
            }
        Log.d(
            TAG_UI,
            "updateRecycler() -> filtered=${filtered.size}/${listaCompleta.size}"
        )
        adapter.updateData(filtered)
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    // ============================ PASSWORD DIALOG FLOW ============================

    /**
     * Mesmo comportamento do JoinGameFragment:
     * se a mesa tiver senha -> mostra dialog custom pedindo senha.
     * Só abre ActivityGameMaster se a senha bater.
     */
    private fun pedirSenhaEAbrirJogo(
        idJogo: Long,
        tituloJogo: String?,
        senhaCorreta: String,
        masterId: Long,
        ativo: Int
    ) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_pedir_senha, null)

        val etSenha = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            R.id.etSenhaSala
        )
        val tvHint = dialogView.findViewById<TextView>(R.id.tvDialogHint)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<TextView>(R.id.btnConfirmar)

        tvHint.text = "Essa mesa é protegida.\nDigite a senha para entrar:"

        val alert = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        alert.setOnShowListener {
            alert.window?.setBackgroundDrawable(
                android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            )
        }

        btnCancelar.setOnClickListener {
            alert.dismiss()
        }

        btnConfirmar.setOnClickListener {
            val digitada = etSenha.text?.toString()?.trim().orEmpty()
            if (digitada == senhaCorreta) {
                abrirGameMaster(
                    idJogo     = idJogo,
                    tituloJogo = tituloJogo,
                    masterId   = masterId,
                    ativo      = ativo
                )
                alert.dismiss()
            } else {
                showCustomToast("Senha incorreta", requireContext())
            }
        }

        alert.show()
    }

    /**
     * Abre a ActivityGameMaster com os mesmos extras que você já passava antes.
     */
    private fun abrirGameMaster(
        idJogo: Long,
        tituloJogo: String?,
        masterId: Long,
        ativo: Int
    ) {
        showCustomToast(
            "Entrando em \"${tituloJogo ?: "Mesa"}\" (id=$idJogo)...",
            requireContext()
        )

        val intent = Intent(requireContext(), ActivityGameMaster::class.java).apply {
            putExtra(ActivityGameMaster.EXTRA_ID_JOGO,   idJogo)
            putExtra(ActivityGameMaster.EXTRA_TITULO,    tituloJogo ?: "")
            putExtra(ActivityGameMaster.EXTRA_MASTER_ID, masterId)
            putExtra(ActivityGameMaster.EXTRA_ATIVO,     ativo)
        }
        startActivity(intent)
    }

    /**
     * Toast estiloso igual JoinGameFragment.
     */
    private fun showCustomToast(message: String, context: Context) {
        val layout = LayoutInflater.from(context)
            .inflate(R.layout.toast_layout, null, false)

        layout.findViewById<TextView>(R.id.toast_message).text = message

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.BOTTOM, 0, 200)
        }.show()
    }
}
