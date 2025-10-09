package com.example.androidapprpg.ui.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import com.example.androidapprpg.R

class LoadingDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(R.layout.dialog_loading)
            setCancelable(false)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { w ->
            // 1) ocupar toda a tela
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            // 2) fundo opaco direto na window (evita “vazamento” por 1 frame)
            w.setBackgroundDrawable(ColorDrawable(Color.BLACK))

            // 3) desenhar por cima das system bars e definir cores
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            w.statusBarColor = Color.BLACK
            w.navigationBarColor = Color.BLACK

            // 4) cutout/notch só existe em API 28+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val attrs = w.attributes
                attrs.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                w.attributes = attrs
            }

            // 5) remover animações da janela (evita piscada)
            w.setWindowAnimations(0)

            // 6) edge-to-edge + esconder barras
            WindowCompat.setDecorFitsSystemWindows(w, false)
            val controller = WindowInsetsControllerCompat(w, w.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.let { w ->
            val controller = WindowInsetsControllerCompat(w, w.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
