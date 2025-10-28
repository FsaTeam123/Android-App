package com.example.androidapprpg.ui.fragment.GameManager

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.androidapprpg.BuildConfig
import com.example.androidapprpg.R
import com.example.androidapprpg.adapter.PlayersAdapter
import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import com.example.androidapprpg.data.model.UsersGameDataModel.UsersGameDataModel
import com.example.androidapprpg.databinding.FragmentGameBinding
import com.example.androidapprpg.ui.activity.ActivityGameMaster
import com.example.androidapprpg.ui.viewmodel.GameCanvasViewModel
import com.example.androidapprpg.ui.viewmodel.GameFragmentViewModel
import com.example.androidapprpg.ui.viewmodel.MapViewModel
import com.example.androidapprpg.ui.widget.GridCanvasView
import com.example.androidapprpg.utils.Result
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GameFragment : Fragment() {

    private val TAG = "GameFragment"

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    // canvas/mapa
    private val vm: MapViewModel by activityViewModels()
    private val canvasVm: GameCanvasViewModel by activityViewModels()

    // jogadores dessa mesa
    private val playersVm: GameFragmentViewModel by viewModels()

    // adapter reutilizável pros jogadores
    private lateinit var playersAdapter: PlayersAdapter

    private var mapTarget: CustomTarget<Bitmap>? = null

    // estilos atuais de desenho
    private var currentStrokeColor = Color.parseColor("#C8A24A")
    private var currentFillColor   = Color.parseColor("#33C8A24A")
    private var currentTextColor   = Color.parseColor("#EED7A1")
    private var currentStrokeWidth = 4f
    private var currentTextSize    = 28f
    private val canvasBg           = Color.parseColor("#121212")

    // último transform salvo
    private var lastScale = 1f
    private var lastOffX  = 0f
    private var lastOffY  = 0f

    // último mapa aplicado
    private var lastAppliedMapId: String? = null

    // vamos guardar a instância do diálogo atual pra conseguir fechar no botão "Fechar"
    private var playersDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // === canvas setup ===
        binding.gridCanvas.apply {
            setGridSize(24f)
            setScaleLimits(min = 0.5f, max = 5f)

            setTool(canvasVm.tool)
            mapAlpha = canvasVm.mapAlpha
            drawGridOnTop = canvasVm.drawGridOnTop

            setStrokeColor(currentStrokeColor)
            setFillColor(currentFillColor)
            setTextColor(currentTextColor)
            setStrokeWidth(currentStrokeWidth)
            setTextSize(currentTextSize)

            transformListener = object : GridCanvasView.OnTransformChangedListener {
                override fun onTransformChanged(scale: Float, offsetX: Float, offsetY: Float) {
                    lastScale = scale
                    lastOffX = offsetX
                    lastOffY = offsetY
                    val mapId = canvasVm.currentMapId
                    Log.d(TAG, "onTransformChanged s=$scale off=($offsetX,$offsetY) -> save mapId=$mapId")
                    canvasVm.saveTransform(mapId, scale, offsetX, offsetY)
                }
            }

            requestTextListener = object : GridCanvasView.OnRequestTextListener {
                override fun onRequestText(x: Float, y: Float) {
                    showTextDialog { typed -> commitText(typed) }
                }
            }
        }

        setupTopBar()
        setupSideToolbar()

        // === botão "ver jogadores" ===
        setupPlayersButton()

        // carrega jogadores já agora (assim o diálogo já abre pronto)
        loadPlayersFromGame()

        // observar troca de mapa e renderizar bitmap + shapes
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.selectedMap.collect { selected ->
                    Log.d(TAG, "selectedMap emit: $selected")

                    when (selected) {
                        null -> {
                            if (lastAppliedMapId == null) {
                                Log.d(TAG, "selectedMap is null, clearing canvas")
                                binding.gridCanvas.setMapBitmap(null)
                                canvasVm.setCurrentMap(null)
                            } else {
                                Log.d(TAG, "selectedMap is null, keeping current canvas (mapId=$lastAppliedMapId)")
                            }
                        }
                        else -> {
                            val newId = mapIdOf(selected)

                            if (lastAppliedMapId != null && lastAppliedMapId != newId) {
                                Log.d(TAG, "switching maps: $lastAppliedMapId -> $newId, saving current canvas")
                                canvasVm.saveCanvasState(
                                    lastAppliedMapId!!,
                                    binding.gridCanvas.exportState()
                                )
                            } else {
                                Log.d(TAG, "same map re-emitted (id=$newId) -> do NOT overwrite saved state")
                            }

                            canvasVm.setCurrentMap(newId)
                            loadBitmapIntoCanvas(selected)
                        }
                    }
                }
            }
        }
    }

    // top bar (dado e chat)
    private fun setupTopBar() = with(binding) {
        btnDice.setOnClickListener {
            DiceBottomSheet().show(childFragmentManager, "DiceBottomSheet")
        }
        btnChat.setOnClickListener {
            val action = GameFragmentDirections.actionGameManagerToChatFragment()
            findNavController().navigate(action)
        }
    }

    /**
     * Configura o clique do botão redondo `btnPlayers` (aquele FrameLayout no canto do mapa).
     * Ao clicar: abre um diálogo customizado com a lista de jogadores.
     */
    private fun setupPlayersButton() = with(binding) {
        btnPlayers.setOnClickListener {
            showPlayersDialog()
        }
    }

    /**
     * Cria e mostra o diálogo de jogadores.
     * - Infla o layout players_dialog.xml
     * - Pluga RecyclerView + Adapter
     * - Observa playersVm.playersResult e atualiza loading/lista
     */
    private fun showPlayersDialog() {
        // infla o layout customizado do diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_players, null)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerPlayers)
        val fecharBtn    = dialogView.findViewById<TextView>(R.id.btnFecharPlayers)

        // cria / seta adapter
        playersAdapter = PlayersAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = playersAdapter
        recyclerView.setHasFixedSize(false)

        // monta o AlertDialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // botão fechar do próprio layout
        fecharBtn.setOnClickListener {
            dialog.dismiss()
        }

        // Observador do LiveData. Ele atualiza a UI do diálogo.
        val observer = object : Observer<Result<*>> {
            @Suppress("UNCHECKED_CAST")
            override fun onChanged(result: Result<*>) {
                when (result) {
                    is Result.Loading -> {

                    }
                    is Result.Success<*> -> {

                        val lista = result.data as? List<*>
                        @Suppress("UNCHECKED_CAST")
                        playersAdapter.submitList(lista as? List<UsersGameDataModel> ?: emptyList())
                    }
                    is Result.Error -> {

                        Log.e(TAG, "Erro carregando jogadores: ${result.message}")
                    }

                    is Result.StopViewModel -> {

                    }
                }
            }
        }

        // começa a observar
        playersVm.playersResult.observe(viewLifecycleOwner, observer)

        // quando o diálogo fechar, remove o observer pra não vazar
        dialog.setOnDismissListener {
            playersVm.playersResult.removeObserver(observer)
            playersDialog = null
        }

        playersDialog = dialog
        dialog.show()
    }

    // pega o idJogo da ActivityGameMaster e dispara o load no ViewModel
    private fun loadPlayersFromGame() {
        val gameId = requireActivity()
            .intent
            .getLongExtra(ActivityGameMaster.EXTRA_ID_JOGO, -1L)

        if (gameId <= 0L) {
            Log.e(TAG, "Game ID inválido. Não foi possível carregar jogadores.")
            return
        }

        playersVm.loadPlayers(gameId)
    }

    // aplica estilos atuais ao canvas
    private fun applyCurrentStyle() = with(binding.gridCanvas) {
        setStrokeColor(currentStrokeColor)
        setFillColor(currentFillColor)
        setTextColor(currentTextColor)
        setStrokeWidth(currentStrokeWidth)
        setTextSize(currentTextSize)
    }

    // carrega o bitmap do mapa atual e restaura estado/shapes
    private fun loadBitmapIntoCanvas(selected: MapDataModel) {
        val mapId = mapIdOf(selected)

        val src: Any = when {
            selected.imageUri != null -> selected.imageUri!!
            !selected.imageUrl.isNullOrBlank() -> selected.imageUrl!!
            else -> "${BuildConfig.BASE_URL_GAME}/mapas/${mapId.toLong()}/imagem"
        }
        Log.d(TAG, "loadBitmapIntoCanvas(mapId=$mapId, src=$src)")

        mapTarget?.let { Glide.with(this).clear(it) }

        mapTarget = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                Log.d(TAG, "onResourceReady(mapId=$mapId, bmp=${resource.width}x${resource.height})")

                // 1) seta o bitmap
                binding.gridCanvas.setMapBitmap(resource)

                // 2) restaura transform salvo
                canvasVm.getTransform(mapId)?.let { saved ->
                    Log.d(TAG, "restoring transform for mapId=$mapId -> $saved")
                    lastScale = saved.scale
                    lastOffX = saved.ox
                    lastOffY = saved.oy
                    binding.gridCanvas.setTransform(
                        saved.scale,
                        saved.ox,
                        saved.oy,
                        notify = false
                    )
                } ?: run {
                    Log.d(TAG, "no transform saved for mapId=$mapId, centering origin")
                    binding.gridCanvas.setMapBitmapCenteredOrigin(resource)
                    lastScale = 1f
                    lastOffX = binding.gridCanvas.width / 2f
                    lastOffY = binding.gridCanvas.height / 2f
                }

                // 3) shapes salvos
                canvasVm.getCanvasState(mapId)?.let { state ->
                    binding.gridCanvas.importState(state)
                } ?: run {
                    binding.gridCanvas.importState(null)
                }

                // 4) prefs extra
                binding.gridCanvas.mapAlpha = canvasVm.mapAlpha

                lastAppliedMapId = mapId
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                Log.d(TAG, "onLoadCleared(mapId=$mapId)")
            }
        }

        Glide.with(this)
            .asBitmap()
            .load(src)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(mapTarget!!)
    }

    // toolbar lateral (desenho / borracha / undo / cor etc.)
    private fun setupSideToolbar() = with(binding) {
        fun select(v: View) {
            listOf(
                btnSelect, btnPan, btnPen, btnLine,
                btnRect, btnCircle, btnText, btnEraser,
                btnUndo, btnRedo, btnColor
            ).forEach { it.isSelected = false }
            v.isSelected = true
        }

        btnSelect.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.SELECT)
            canvasVm.setTool(GridCanvasView.Tool.SELECT)
            applyCurrentStyle(); select(it)
        }
        btnPan.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.PAN)
            canvasVm.setTool(GridCanvasView.Tool.PAN)
            applyCurrentStyle(); select(it)
        }
        btnPen.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.PEN)
            canvasVm.setTool(GridCanvasView.Tool.PEN)
            applyCurrentStyle(); select(it)
        }
        btnLine.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.LINE)
            canvasVm.setTool(GridCanvasView.Tool.LINE)
            applyCurrentStyle(); select(it)
        }
        btnRect.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.RECT)
            canvasVm.setTool(GridCanvasView.Tool.RECT)
            applyCurrentStyle(); select(it)
        }
        btnCircle.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.CIRCLE)
            canvasVm.setTool(GridCanvasView.Tool.CIRCLE)
            applyCurrentStyle(); select(it)
        }
        btnText.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.TEXT)
            canvasVm.setTool(GridCanvasView.Tool.TEXT)
            applyCurrentStyle(); select(it)
        }
        btnEraser.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.ERASER)
            canvasVm.setTool(GridCanvasView.Tool.ERASER)
            applyCurrentStyle(); select(it)
        }

        // segurar a borracha => vira "pincel com cor do fundo" pra apagar
        btnEraser.setOnLongClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.PEN)
            canvasVm.setTool(GridCanvasView.Tool.PEN)
            currentStrokeColor = canvasBg
            gridCanvas.setStrokeColor(currentStrokeColor)
            applyCurrentStyle(); select(it)
            true
        }

        btnUndo.setOnClickListener { gridCanvas.undo() }
        btnRedo.setOnClickListener { gridCanvas.redo() }

        btnPen.setOnLongClickListener {
            showStrokeQuickActions(
                onPickColor = {
                    pickColor(currentStrokeColor) { c ->
                        currentStrokeColor = c
                        gridCanvas.setStrokeColor(c)
                        applyCurrentStyle()
                    }
                },
                onPickWidth = {
                    pickStrokeWidth(currentStrokeWidth) { w ->
                        currentStrokeWidth = w
                        gridCanvas.setStrokeWidth(w)
                        applyCurrentStyle()
                    }
                }
            )
            true
        }
        btnLine.setOnLongClickListener { btnPen.performLongClick() }

        val shapeLongClick = View.OnLongClickListener {
            showShapeQuickActions(
                onPickStroke = {
                    pickColor(currentStrokeColor) { c ->
                        currentStrokeColor = c
                        gridCanvas.setStrokeColor(c)
                        applyCurrentStyle()
                    }
                },
                onPickFill = {
                    pickColor(currentFillColor) { c ->
                        currentFillColor = c
                        gridCanvas.setFillColor(c)
                        applyCurrentStyle()
                    }
                },
                onPickWidth = {
                    pickStrokeWidth(currentStrokeWidth) { w ->
                        currentStrokeWidth = w
                        gridCanvas.setStrokeWidth(w)
                        applyCurrentStyle()
                    }
                }
            )
            true
        }
        btnRect.setOnLongClickListener(shapeLongClick)
        btnCircle.setOnLongClickListener(shapeLongClick)

        btnText.setOnLongClickListener {
            showTextQuickActions(
                onPickColor = {
                    pickColor(currentTextColor) { c ->
                        currentTextColor = c
                        gridCanvas.setTextColor(c)
                        applyCurrentStyle()
                    }
                },
                onPickSize = {
                    pickTextSize(currentTextSize) { s ->
                        currentTextSize = s
                        gridCanvas.setTextSize(s)
                        applyCurrentStyle()
                    }
                }
            )
            true
        }

        btnColor.setOnClickListener { showGlobalPalette() }
    }

    // === diálogos auxiliares de cor/tamanho texto etc. ===
    private fun showGlobalPalette() = with(binding) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Paleta")
            .setItems(arrayOf("Cor do traço", "Cor de preenchimento", "Cor do texto")) { d, which ->
                when (which) {
                    0 -> pickColor(currentStrokeColor) { c ->
                        currentStrokeColor = c
                        gridCanvas.setStrokeColor(c)
                        applyCurrentStyle()
                    }
                    1 -> pickColor(currentFillColor) { c ->
                        currentFillColor = c
                        gridCanvas.setFillColor(c)
                        applyCurrentStyle()
                    }
                    2 -> pickColor(currentTextColor) { c ->
                        currentTextColor = c
                        gridCanvas.setTextColor(c)
                        applyCurrentStyle()
                    }
                }
                d.dismiss()
            }.show()
    }

    private fun showStrokeQuickActions(onPickColor: () -> Unit, onPickWidth: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Traço")
            .setItems(arrayOf("Cor", "Espessura")) { d, which ->
                when (which) {
                    0 -> onPickColor()
                    1 -> onPickWidth()
                }
                d.dismiss()
            }
            .show()
    }

    private fun showShapeQuickActions(
        onPickStroke: () -> Unit,
        onPickFill: () -> Unit,
        onPickWidth: () -> Unit
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Forma")
            .setItems(arrayOf("Cor do traço", "Cor de preenchimento", "Espessura")) { d, which ->
                when (which) {
                    0 -> onPickStroke()
                    1 -> onPickFill()
                    2 -> onPickWidth()
                }
                d.dismiss()
            }.show()
    }

    private fun showTextQuickActions(onPickColor: () -> Unit, onPickSize: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Texto")
            .setItems(arrayOf("Cor", "Tamanho")) { d, which ->
                when (which) {
                    0 -> onPickColor()
                    1 -> onPickSize()
                }
                d.dismiss()
            }
            .show()
    }

    private fun pickColor(current: Int, onPick: (Int) -> Unit) {
        val colors = intArrayOf(
            Color.WHITE, Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.CYAN, Color.MAGENTA,
            Color.parseColor("#C8A24A"),
            Color.parseColor("#EED7A1"),
            Color.parseColor("#33C8A24A"),
            Color.parseColor("#121212")
        )
        val names = arrayOf(
            "Branco", "Preto", "Vermelho", "Verde", "Azul",
            "Amarelo", "Ciano", "Magenta",
            "Dourado", "Texto padrão", "Fill padrão", "Fundo"
        )
        val sel = colors.indexOf(current).let { if (it >= 0) it else 0 }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Escolha a cor")
            .setSingleChoiceItems(names, sel) { dialog, which ->
                onPick(colors[which])
                dialog.dismiss()
            }.show()
    }

    private fun pickStrokeWidth(current: Float, onPick: (Float) -> Unit) {
        val widths = floatArrayOf(2f, 4f, 6f, 8f, 12f)
        val labels = arrayOf("2 px", "4 px", "6 px", "8 px", "12 px")
        val sel = widths.indexOfFirst { kotlin.math.abs(it - current) < 0.001f }
            .let { if (it >= 0) it else 1 }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Espessura do traço")
            .setSingleChoiceItems(labels, sel) { dialog, which ->
                onPick(widths[which])
                dialog.dismiss()
            }.show()
    }

    private fun pickTextSize(current: Float, onPick: (Float) -> Unit) {
        val sizes = floatArrayOf(18f, 22f, 26f, 28f, 32f, 36f, 42f)
        val labels = arrayOf("18", "22", "26", "28", "32", "36", "42")
        val sel = sizes.indexOfFirst { kotlin.math.abs(it - current) < 0.001f }
            .let { if (it >= 0) it else 3 }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tamanho do texto")
            .setSingleChoiceItems(labels, sel) { dialog, which ->
                onPick(sizes[which])
                dialog.dismiss()
            }.show()
    }

    private fun showTextDialog(onConfirm: (String) -> Unit) {
        val input = EditText(requireContext()).apply {
            hint = "Digite o texto"
            setTextColor(Color.WHITE)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Novo texto")
            .setView(input)
            .setPositiveButton("OK") { d, _ ->
                input.text?.toString()?.let { if (it.isNotBlank()) onConfirm(it) }
                d.dismiss()
            }
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .show()
    }

    private fun mapIdOf(m: MapDataModel): String = m.id

    override fun onPause() {
        super.onPause()
        // salva estado atual do canvas pro mapa atual
        canvasVm.currentMapId?.let { id ->
            canvasVm.saveCanvasState(id, binding.gridCanvas.exportState())
            canvasVm.saveTransform(id, lastScale, lastOffX, lastOffY)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // se o diálogo ainda estiver aberto, fecha
        playersDialog?.dismiss()
        playersDialog = null

        mapTarget?.let { Glide.with(this).clear(it) }
        mapTarget = null
        _binding = null
    }
}
