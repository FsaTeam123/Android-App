package com.example.androidapprpg.ui.fragment.GameManager

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import com.example.androidapprpg.R
import com.example.androidapprpg.dicecore.DiceSpec
import com.example.androidapprpg.dicecore.RollMode
import com.example.androidapprpg.dicecore.TipoDado
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText

// Você pode manter o mesmo layout (bottom_sheet_dice)
// ou renomear para dialog_dice.xml. Aqui aproveitei o mesmo.
class DiceSelectDialog : DialogFragment(R.layout.bottom_sheet_dice) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // usa o Dialog padrão e apenas ajusta a janela no onStart
        return Dialog(requireContext())
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { w ->
            // largura ~92% da tela, altura wrap
            val width  = (resources.displayMetrics.widthPixels * 0.92f).toInt()
            w.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
            w.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // teclado: ajusta altura do dialog
            w.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            )

            // opcional: imersivo, se quiser esconder system bars enquanto o dialog está aberto
            WindowCompat.setDecorFitsSystemWindows(w, false)
            val ic = WindowInsetsControllerCompat(w, w.decorView)
            ic.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            ic.hide(WindowInsetsCompat.Type.systemBars())
        }

        // bind de UI (a root do seu layout)
        val root = dialog?.findViewById<View>(R.id.dicePanelRoot) ?: return

        // Seleciona "Normal" por padrão caso nada esteja marcado
        root.findViewById<MaterialButton>(R.id.btnNorm)?.isChecked = true

        // LIMPAR
        root.findViewById<View>(R.id.btnClear)?.setOnClickListener {
            intArrayOf(
                R.id.tgD4, R.id.tgD6, R.id.tgD8, R.id.tgD10,
                R.id.tgD12, R.id.tgD20, R.id.tgD100, R.id.tgFudge
            ).forEach { id ->
                root.findViewById<MaterialButtonToggleGroup>(id)?.clearChecked()
            }
            root.findViewById<TextInputEditText>(R.id.etModifier)?.setText("")
            root.findViewById<MaterialButton>(R.id.btnNorm)?.isChecked = true
        }

        // ROLAR
        root.findViewById<View>(R.id.btnRollNow)?.setOnClickListener {
            val spec = buildDiceSpecFromPanel(root)
            Log.d("Dice", "specGerado=$spec")
            parentFragmentManager.setFragmentResult(
                "dice_spec_request",
                bundleOf("spec" to spec)
            )
            dismiss()
        }
    }

    // ==== helpers iguais aos seus ====

    private fun MaterialButtonToggleGroup.selectedIntOrZero(): Int {
        val id = checkedButtonId
        if (id == -1) return 0
        val btn = findViewById<MaterialButton>(id) ?: return 0
        return (btn.tag as? String)?.toIntOrNull()
            ?: btn.text?.toString()?.toIntOrNull()
            ?: 0
    }

    private fun readRollMode(root: View): RollMode {
        val tg = root.findViewById<MaterialButtonToggleGroup>(R.id.tgAdvantage)
        return when (tg?.checkedButtonId) {
            R.id.btnAdv -> RollMode.VANTAGEM
            R.id.btnDis -> RollMode.DESVANTAGEM
            else -> RollMode.NORMAL
        }
    }

    private fun countOf(root: View, groupId: Int) =
        root.findViewById<MaterialButtonToggleGroup>(groupId)?.selectedIntOrZero() ?: 0

    private fun buildDiceSpecFromPanel(root: View): DiceSpec {
        val counts = buildMap {
            put(TipoDado.D4,    countOf(root, R.id.tgD4))
            put(TipoDado.D6,    countOf(root, R.id.tgD6))
            put(TipoDado.D8,    countOf(root, R.id.tgD8))
            put(TipoDado.D10,   countOf(root, R.id.tgD10))
            put(TipoDado.D12,   countOf(root, R.id.tgD12))
            put(TipoDado.D20,   countOf(root, R.id.tgD20))
            put(TipoDado.D100,  countOf(root, R.id.tgD100))
            put(TipoDado.FUDGE, countOf(root, R.id.tgFudge))
        }.filterValues { it > 0 }

        val modifier = root.findViewById<TextInputEditText>(R.id.etModifier)
            ?.text?.toString()?.toIntOrNull() ?: 0

        return DiceSpec(
            counts = counts,
            modifier = modifier,
            rollMode = readRollMode(root),
            seed = System.nanoTime()
        )
    }
}
