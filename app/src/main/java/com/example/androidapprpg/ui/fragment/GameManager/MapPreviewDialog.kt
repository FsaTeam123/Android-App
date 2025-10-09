// MapPreviewDialog.kt
package com.example.androidapprpg.ui.fragment.GameManager

import android.app.Dialog
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.DialogMapPreviewBinding
import kotlinx.parcelize.Parcelize

class MapPreviewDialog : DialogFragment() {

    private var _binding: DialogMapPreviewBinding? = null
    private val binding get() = _binding!!

    @Parcelize
    data class Args(
        val title: String,
        val imageUri: Uri? = null,
        val imageUrl: String? = null,
        val heightFraction: Float = 0.6f   // 60% da tela (ajuste à vontade)
    ) : Parcelable

    private lateinit var args: Args

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = requireArguments().getParcelableCompat(KEY_ARGS)!!
        setStyle(STYLE_NO_TITLE, R.style.AppDialog) // usa nosso tema
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState)
        d.setOnShowListener {
            // tamanho do diálogo
            val w = d.window ?: return@setOnShowListener
            val dm = resources.displayMetrics
            w.setLayout(
                (dm.widthPixels * 0.94f).toInt(),                     // quase full width
                (dm.heightPixels * args.heightFraction).toInt()       // fração da altura
            )
            w.setBackgroundDrawableResource(R.drawable.dialog_container_bg_map) // fundo opaco arredondado
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            enterImmersive(w)
        }
        return d
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = DialogMapPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.txtTitle.text = args.title

        val src: Any = args.imageUri ?: args.imageUrl ?: R.drawable.sample_map
        Glide.with(binding.previewMap)
            .load(src)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.drawable.sample_map)
            .error(R.drawable.sample_map)
            .into(binding.previewMap)

        binding.fabZoomIn.setOnClickListener  { binding.previewMap.zoomIn() }
        binding.fabZoomOut.setOnClickListener { binding.previewMap.zoomOut() }
        binding.btnClose.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun enterImmersive(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        val ic = WindowInsetsControllerCompat(window, window.decorView)
        ic.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ic.hide(WindowInsetsCompat.Type.systemBars())
    }

    companion object {
        private const val KEY_ARGS = "args"
        fun newInstance(args: Args) = MapPreviewDialog().apply {
            arguments = bundleOf(KEY_ARGS to args)
        }
    }
}

private inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? =
    if (Build.VERSION.SDK_INT >= 33) getParcelable(key, T::class.java)
    else @Suppress("DEPRECATION") getParcelable(key)
