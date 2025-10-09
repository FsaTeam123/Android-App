package com.example.androidapprpg.ui.fragment.GameManager

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.androidapprpg.R
import com.example.androidapprpg.data.model.MapDataModel.MapDataModel
import com.example.androidapprpg.databinding.FragmentGameBinding
import com.example.androidapprpg.dicecore.DiceSpec
import com.example.androidapprpg.ui.viewmodel.MapViewModel
import com.example.androidapprpg.ui.widget.GridCanvasView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private val vm: MapViewModel by activityViewModels()

    private var mapTarget: CustomTarget<Bitmap>? = null

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
            setScaleLimits(min = 0.5f, max = 5f)
            setTool(GridCanvasView.Tool.PAN)
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

        // Renderiza o mapa selecionado
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.selectedMap.collect { selected ->
                    if (selected == null) {
                        binding.gridCanvas.setMapBitmap(null)
                    } else {
                        loadBitmapIntoCanvas(selected)
                    }
                }
            }
        }
    }

    private fun setupTopBar() = with(binding) {
        btnDice.setOnClickListener { DiceBottomSheet().show(childFragmentManager, "DiceBottomSheet") }
        btnChat.setOnClickListener {
            val action = GameFragmentDirections.actionGameManagerToChatFragment()
            findNavController().navigate(action)
        }
    }

    private fun applyCurrentStyle() = with(binding.gridCanvas) {
        setStrokeColor(currentStrokeColor)
        setFillColor(currentFillColor)
        setTextColor(currentTextColor)
        setStrokeWidth(currentStrokeWidth)
        setTextSize(currentTextSize)
    }

    private fun loadBitmapIntoCanvas(selected: MapDataModel) {
        val src: Any = when {
            selected.imageUri != null -> selected.imageUri!!
            !selected.imageUrl.isNullOrBlank() -> selected.imageUrl!!
            else -> R.drawable.sample_map
        }

        mapTarget?.let { Glide.with(this).clear(it) }

        mapTarget = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                binding.gridCanvas.setMapBitmap(resource)
                binding.gridCanvas.mapAlpha = 255
                binding.gridCanvas.refitMapToView()
            }
            override fun onLoadCleared(placeholder: Drawable?) {
                binding.gridCanvas.setMapBitmap(null)
            }
        }

        Glide.with(this)
            .asBitmap()
            .load(src)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(mapTarget!!)
    }

    private fun setupSideToolbar() = with(binding) {
        fun select(v: View) {
            listOf(btnSelect, btnPan, btnPen, btnLine, btnRect, btnCircle, btnText, btnEraser, btnUndo, btnRedo)
                .forEach { it.isSelected = false }
            v.isSelected = true
        }

        btnSelect.setOnClickListener { gridCanvas.setTool(GridCanvasView.Tool.SELECT); applyCurrentStyle(); select(it) }
        btnPan.setOnClickListener    { gridCanvas.setTool(GridCanvasView.Tool.PAN);    applyCurrentStyle(); select(it) }
        btnPen.setOnClickListener    { gridCanvas.setTool(GridCanvasView.Tool.PEN);    applyCurrentStyle(); select(it) }
        btnLine.setOnClickListener   { gridCanvas.setTool(GridCanvasView.Tool.LINE);   applyCurrentStyle(); select(it) }
        btnRect.setOnClickListener   { gridCanvas.setTool(GridCanvasView.Tool.RECT);   applyCurrentStyle(); select(it) }
        btnCircle.setOnClickListener { gridCanvas.setTool(GridCanvasView.Tool.CIRCLE); applyCurrentStyle(); select(it) }
        btnText.setOnClickListener   { gridCanvas.setTool(GridCanvasView.Tool.TEXT);   applyCurrentStyle(); select(it) }
        btnEraser.setOnClickListener { gridCanvas.setTool(GridCanvasView.Tool.ERASER); applyCurrentStyle(); select(it) }

        btnEraser.setOnLongClickListener {
            gridCanvas.setTool(GridCanvasView.Tool.PEN)
            currentStrokeColor = canvasBg
            gridCanvas.setStrokeColor(currentStrokeColor)
            applyCurrentStyle(); select(it); true
        }

        btnUndo.setOnClickListener { gridCanvas.undo() }
        btnRedo.setOnClickListener { gridCanvas.redo() }

        btnPen.setOnLongClickListener {
            showStrokeQuickActions(
                onPickColor = { pickColor(currentStrokeColor) { c -> currentStrokeColor = c; gridCanvas.setStrokeColor(c); applyCurrentStyle() } },
                onPickWidth = { pickStrokeWidth(currentStrokeWidth) { w -> currentStrokeWidth = w; gridCanvas.setStrokeWidth(w); applyCurrentStyle() } }
            ); true
        }
        btnLine.setOnLongClickListener { btnPen.performLongClick() }

        val shapeLongClick = View.OnLongClickListener {
            showShapeQuickActions(
                onPickStroke = { pickColor(currentStrokeColor) { c -> currentStrokeColor = c; gridCanvas.setStrokeColor(c); applyCurrentStyle() } },
                onPickFill   = { pickColor(currentFillColor)   { c -> currentFillColor   = c; gridCanvas.setFillColor(c);   applyCurrentStyle() } },
                onPickWidth  = { pickStrokeWidth(currentStrokeWidth) { w -> currentStrokeWidth = w; gridCanvas.setStrokeWidth(w); applyCurrentStyle() } }
            ); true
        }
        btnRect.setOnLongClickListener(shapeLongClick)
        btnCircle.setOnLongClickListener(shapeLongClick)

        btnText.setOnLongClickListener {
            showTextQuickActions(
                onPickColor = { pickColor(currentTextColor) { c -> currentTextColor = c; gridCanvas.setTextColor(c); applyCurrentStyle() } },
                onPickSize  = { pickTextSize(currentTextSize) { s -> currentTextSize = s; gridCanvas.setTextSize(s); applyCurrentStyle() } }
            ); true
        }

        btnColor.setOnClickListener { showGlobalPalette() }
    }

    private fun showGlobalPalette() = with(binding) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Paleta")
            .setItems(arrayOf("Cor do traço", "Cor de preenchimento", "Cor do texto")) { d, which ->
                when (which) {
                    0 -> pickColor(currentStrokeColor) { c -> currentStrokeColor = c; gridCanvas.setStrokeColor(c); applyCurrentStyle() }
                    1 -> pickColor(currentFillColor)   { c -> currentFillColor   = c; gridCanvas.setFillColor(c);   applyCurrentStyle() }
                    2 -> pickColor(currentTextColor)   { c -> currentTextColor   = c; gridCanvas.setTextColor(c);   applyCurrentStyle() }
                }
                d.dismiss()
            }.show()
    }

    private fun showStrokeQuickActions(onPickColor: () -> Unit, onPickWidth: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Traço")
            .setItems(arrayOf("Cor", "Espessura")) { d, which ->
                when (which) { 0 -> onPickColor(); 1 -> onPickWidth() }
                d.dismiss()
            }
            .show()
    }

    private fun showShapeQuickActions(onPickStroke: () -> Unit, onPickFill: () -> Unit, onPickWidth: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Forma")
            .setItems(arrayOf("Cor do traço", "Cor de preenchimento", "Espessura")) { d, which ->
                when (which) { 0 -> onPickStroke(); 1 -> onPickFill(); 2 -> onPickWidth() }
                d.dismiss()
            }.show()
    }

    private fun showTextQuickActions(onPickColor: () -> Unit, onPickSize: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Texto")
            .setItems(arrayOf("Cor", "Tamanho")) { d, which ->
                when (which) { 0 -> onPickColor(); 1 -> onPickSize() }
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
                onPick(colors[which]); dialog.dismiss()
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
                onPick(widths[which]); dialog.dismiss()
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
                onPick(sizes[which]); dialog.dismiss()
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

    override fun onDestroyView() {
        super.onDestroyView()
        mapTarget?.let { Glide.with(this).clear(it) }
        mapTarget = null
        _binding = null
    }
}
