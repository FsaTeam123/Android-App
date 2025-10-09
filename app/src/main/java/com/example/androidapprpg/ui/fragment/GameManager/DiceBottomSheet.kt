package com.example.androidapprpg.ui.fragment.GameManager

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class DiceBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_dice2) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        d.setOnShowListener {
            d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { bs ->
                val behavior = BottomSheetBehavior.from(bs)
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                // altura ~38% da tela
                bs.layoutParams.height = (resources.displayMetrics.heightPixels * 0.38f).toInt()
                bs.requestLayout()

                // remove fundo branco do container
                bs.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                androidx.core.view.ViewCompat.setBackgroundTintList(bs, null)
            }
            d.window?.let { w ->
                WindowCompat.setDecorFitsSystemWindows(w, false)
                w.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                )
                enterImmersive(w)
            }
        }
        return d
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi(view)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.let { enterImmersive(it) }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.window?.let { enterImmersive(it) }
    }

    private fun enterImmersive(w: Window) {
        val ic = WindowInsetsControllerCompat(w, w.decorView)
        ic.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ic.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setUpUi(view: View) {
        val btnRoll  = view.findViewById<MaterialButton>(R.id.btnRollNow)
        val iconWrap = view.findViewById<FrameLayout>(R.id.diceIconWrap)
        val tagline  = view.findViewById<TextView>(R.id.tvTagline)

        // Frase opcional
        tagline.text = listOf(
            "Role os dados e faça história.",
            "Que o crítico venha em boa hora.",
            "A sorte favorece os ousados."
        ).random()

        // Ícone dispara o mesmo clique do botão
        iconWrap.setOnClickListener { btnRoll.performClick() }

        btnRoll.setOnClickListener {

            requireParentFragment().findNavController()
                .navigate(R.id.dicePlayFragment)

            dismiss() // fecha o sheet após navegar
        }
    }

}
