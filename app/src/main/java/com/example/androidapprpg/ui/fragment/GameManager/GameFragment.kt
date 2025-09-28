package com.example.androidapprpg.ui.fragment.GameManager

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.databinding.FragmentGameBinding
import com.example.androidapprpg.ui.bottomsheet.DiceBottomSheet
import com.example.androidapprpg.ui.widget.GridCanvasView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    // estado atual (espelha o estado do GridCanvasView para facilitar seleção inicial)
    private var currentStrokeColor = Color.parseColor("#C8A24A")
    private var currentFillColor   = Color.parseColor("#33C8A24A")
    private var currentTextColor   = Color.parseColor("#EED7A1")
    private var currentStrokeWidth = 4f
    private var currentTextSize    = 28f
    private val canvasBg           = Color.parseColor("#121212")

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

        binding.gridCanvas.apply {
            setGridSize(24f)
            setScaleLimits(0.5f, 5f)
            setTool(GridCanvasView.Tool.PAN)

            // aplica estilo inicial
            setStrokeColor(currentStrokeColor)
            setFillColor(currentFillColor)
            setTextColor(currentTextColor)
            setStrokeWidth(currentStrokeWidth)
            setTextSize(currentTextSize)

            requestTextListener = object : GridCanvasView.OnRequestTextListener {
                override fun onRequestText(x: Float, y: Float) {
                    showTextDialog { typed -> commitText(typed) }
                }
            }
        }

        setupTopBar()
        setupSideToolbar()
    }

    // Helper: reaplica o estilo atual no GridCanvasView
    private fun applyCurrentStyle() = with(binding.gridCanvas) {
        setStrokeColor(currentStrokeColor)
        setFillColor(currentFillColor)
        setTextColor(currentTextColor)
        setStrokeWidth(currentStrokeWidth)
        setTextSize(currentTextSize)
    }

    // -------- Top bar --------
    private fun setupTopBar() = with(binding) {
        btnDice.setOnClickListener {
            DiceBottomSheet().show(childFragmentManager, "DiceBottomSheet")
        }
        btnChat.setOnClickListener {
            val action = GameFragmentDirections.actionGameManagerToChatFragment()
            findNavController().navigate(action)
        }
    }

    // -------- Side toolbar --------
    private fun setupSideToolbar() = with(binding) {
        fun select(v: View) {
            listOf(
                btnSelect,
                btnPan, btnPen, btnLine, btnRect, btnCircle, btnText, btnEraser,
                btnUndo, btnRedo
            ).forEach { it.isSelected = false }
            v.isSelected = true
        }

        // Selecionar / mover
        btnSelect.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.SELECT)
            applyCurrentStyle()
            select(it)
        }

        // Ferramentas (clique)
        btnPan.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.PAN)
            applyCurrentStyle()
            select(it)
        }
        btnPen.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.PEN)
            applyCurrentStyle()
            select(it)
        }
        btnLine.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.LINE)
            applyCurrentStyle()
            select(it)
        }
        btnRect.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.RECT)
            applyCurrentStyle()
            select(it)
        }
        btnCircle.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.CIRCLE)
            applyCurrentStyle()
            select(it)
        }
        btnText.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.TEXT)
            applyCurrentStyle()
            select(it)
        }

        // Borracha: modo que remove shapes
        btnEraser.setOnClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.ERASER)
            applyCurrentStyle()
            select(it)
        }
        // (opcional) Long click para “pintar com fundo” como borracha suave
        btnEraser.setOnLongClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.PEN)
            currentStrokeColor = canvasBg
            gridCanvas.setStrokeColor(currentStrokeColor)
            applyCurrentStyle()
            select(it)
            true
        }

        // Desfazer/Refazer
        btnUndo.setOnClickListener { gridCanvas.undo() }
        btnRedo.setOnClickListener { gridCanvas.redo() }

        // Atalhos por long-click
        // Caneta/linha: cor do traço ou espessura
        btnPen.setOnLongClickListener {
            showStrokeQuickActions(
                onPickColor = {
                    pickColor(currentStrokeColor) { color ->
                        currentStrokeColor = color
                        gridCanvas.setStrokeColor(color)
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
            ); true
        }
        btnLine.setOnLongClickListener { btnPen.performLongClick() }

        // Retângulo/Círculo: cor do traço, cor do preenchimento, espessura
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
            ); true
        }
        btnRect.setOnLongClickListener(shapeLongClick)
        btnCircle.setOnLongClickListener(shapeLongClick)

        // Texto: cor e tamanho
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
            ); true
        }

        // Paleta global — sempre permite trocar cor do traço, fill e texto (para próximos)
        btnColor.setOnClickListener { showGlobalPalette() }
    }

    // --------- Paleta Global (próximos desenhos) ---------
    private fun showGlobalPalette() = with(binding) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Paleta")
            .setItems(arrayOf(
                "Cor do traço",
                "Cor de preenchimento",
                "Cor do texto"
            )) { d, which ->
                when (which) {
                    0 -> { // stroke
                        pickColor(currentStrokeColor) { c ->
                            currentStrokeColor = c
                            gridCanvas.setStrokeColor(c)
                            applyCurrentStyle()
                        }
                    }
                    1 -> { // fill
                        pickColor(currentFillColor) { c ->
                            currentFillColor = c
                            gridCanvas.setFillColor(c)
                            applyCurrentStyle()
                        }
                    }
                    2 -> { // text color (o importante p/ seu caso)
                        pickColor(currentTextColor) { c ->
                            currentTextColor = c
                            gridCanvas.setTextColor(c)
                            applyCurrentStyle()
                        }
                    }
                }
                d.dismiss()
            }
            .show()
    }

    // --------- Diálogos rápidos ---------
    private fun showStrokeQuickActions(
        onPickColor: () -> Unit,
        onPickWidth: () -> Unit
    ) {
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
            }
            .show()
    }

    private fun showTextQuickActions(
        onPickColor: () -> Unit,
        onPickSize: () -> Unit
    ) {
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

    // --------- Seletores (cor/espessura/tamanho) ---------
    private fun pickColor(current: Int, onPick: (Int) -> Unit) {
        val colors = intArrayOf(
            Color.WHITE, Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.CYAN, Color.MAGENTA,
            Color.parseColor("#C8A24A"),          // dourado
            Color.parseColor("#EED7A1"),          // texto padrão
            Color.parseColor("#33C8A24A"),        // fill padrão
            Color.parseColor("#121212")           // fundo (p/ “borracha pintar”)
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
            }
            .show()
    }

    private fun pickStrokeWidth(current: Float, onPick: (Float) -> Unit) {
        val widths = floatArrayOf(2f, 4f, 6f, 8f, 12f)
        val labels = arrayOf("2 px", "4 px", "6 px", "8 px", "12 px")
        val sel = widths.indexOfFirst { abs(it - current) < 0.001f }.let { if (it >= 0) it else 1 }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Espessura do traço")
            .setSingleChoiceItems(labels, sel) { dialog, which ->
                onPick(widths[which])
                dialog.dismiss()
            }
            .show()
    }

    private fun pickTextSize(current: Float, onPick: (Float) -> Unit) {
        val sizes = floatArrayOf(18f, 22f, 26f, 28f, 32f, 36f, 42f)
        val labels = arrayOf("18", "22", "26", "28", "32", "36", "42")
        val sel = sizes.indexOfFirst { abs(it - current) < 0.001f }.let { if (it >= 0) it else 3 }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tamanho do texto")
            .setSingleChoiceItems(labels, sel) { dialog, which ->
                onPick(sizes[which])
                dialog.dismiss()
            }
            .show()
    }

    // --------- Diálogo para inserir texto ---------
    private fun showTextDialog(onConfirm: (String) -> Unit) {
        val input = EditText(requireContext()).apply {
            hint = "Digite o texto"
            setTextColor(Color.WHITE)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Novo texto")
            .setView(input)
            .setPositiveButton("OK") { d, _ ->
                val t = input.text?.toString().orEmpty()
                if (t.isNotBlank()) onConfirm(t)
                d.dismiss()
            }
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
