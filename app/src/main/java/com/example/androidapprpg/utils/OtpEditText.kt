package com.example.androidapprpg.utils

import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText


class OtpEditText @JvmOverloads constructor( context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.editTextStyle) : AppCompatEditText(context, attrs, defStyleAttr) {

    var onPaste: ((String) -> Unit)? = null

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == android.R.id.paste || id == android.R.id.pasteAsPlainText) {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = cm.primaryClip
            val pasted = clip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
            if (pasted.isNotBlank()) {
                onPaste?.invoke(pasted)
                return true // já tratamos o colar
            }
        }
        return super.onTextContextMenuItem(id)
    }
}