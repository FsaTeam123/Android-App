
package com.example.androidapprpg.ui.bottomsheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidapprpg.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DiceBottomSheet : BottomSheetDialogFragment(R.layout.panel_dice) {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        d.setOnShowListener {

            d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { bs ->
                val behavior = BottomSheetBehavior.from(bs)
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                // ~85% da altura da tela
                bs.layoutParams.height = (resources.displayMetrics.heightPixels * 0.85f).toInt()
                bs.requestLayout()
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

    override fun onResume() {
        super.onResume()
        // Alguns devices “desfazem” o imersivo ao abrir o diálogo: reaplique
        dialog?.window?.let { enterImmersive(it) }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Garante que a Activity do jogo continue imersiva ao fechar a sheet
        requireActivity().window.let { enterImmersive(it) }
    }

    private fun enterImmersive(w: Window) {
        val ic = WindowInsetsControllerCompat(w, w.decorView)
        ic.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ic.hide(WindowInsetsCompat.Type.systemBars())
    }
}
