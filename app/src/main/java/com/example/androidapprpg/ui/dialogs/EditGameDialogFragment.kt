package com.example.androidapprpg.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.androidapprpg.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class EditGameDialogFragment : DialogFragment() {

    companion object {
        private const val TAG = "UI:EditGameDialog"

        // ----- ARGS (entradas do diálogo) -----
        const val ARG_IDJOGO      = "arg_idJogo"
        const val ARG_TITULO      = "arg_titulo"
        const val ARG_QTD         = "arg_qtd"
        const val ARG_NIVEL       = "arg_nivel"
        const val ARG_SENHA       = "arg_senha"
        const val ARG_ATIVO       = "arg_ativo"

        // IDs fixos que reaproveitamos do response
        const val ARG_MASTER_ID   = "arg_masterId"
        const val ARG_ESPECIFIC   = "arg_isEspecificClass"
        const val ARG_TIPO_ID     = "arg_tipoJogoId"
        const val ARG_GERACAO_ID  = "arg_geracaoMundoId"
        const val ARG_ESTILO_ID   = "arg_estiloCampanhaId"
        const val ARG_HISTORIA_ID = "arg_historiaId"
        const val ARG_TEMA_ID     = "arg_temaId"

        // ----- RESULT (saída do diálogo) -----
        const val REQ_KEY     = "edit_game_result"
        const val RES_IDJOGO  = "res_idJogo"
        const val RES_TITULO  = "res_titulo"
        const val RES_QTD     = "res_qtd"
        const val RES_NIVEL   = "res_nivel"
        const val RES_SENHA   = "res_senha"
        const val RES_ATIVO   = "res_ativo"

        fun newInstance(
            idJogo: Long,
            titulo: String,
            qtd: Int,
            nivel: Int,
            senha: String?,
            ativo: Int,
            masterId: Long,
            isEspecificClass: Int,
            tipoId: Long,
            geracaoId: Long,
            estiloId: Long,
            historiaId: Long,
            temaId: Long
        ) = EditGameDialogFragment().apply {
            arguments = bundleOf(
                ARG_IDJOGO      to idJogo,
                ARG_TITULO      to titulo,
                ARG_QTD         to qtd,
                ARG_NIVEL       to nivel,
                ARG_SENHA       to (senha ?: ""),
                ARG_ATIVO       to ativo,
                ARG_MASTER_ID   to masterId,
                ARG_ESPECIFIC   to isEspecificClass,
                ARG_TIPO_ID     to tipoId,
                ARG_GERACAO_ID  to geracaoId,
                ARG_ESTILO_ID   to estiloId,
                ARG_HISTORIA_ID to historiaId,
                ARG_TEMA_ID     to temaId
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog() - args=${arguments?.keySet()?.joinToString()}")

        val ctx = requireContext()
        val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_edit_game, null, false)

        // ---- binds ----
        val etTitulo = view.findViewById<TextInputEditText>(R.id.etTitulo)
        val etNivel  = view.findViewById<TextInputEditText>(R.id.etNivel)
        val etQtd    = view.findViewById<TextInputEditText>(R.id.etQtd)
        val etSenha  = view.findViewById<TextInputEditText>(R.id.etSenha)
        val swAtivo  = view.findViewById<SwitchMaterial>(R.id.swAtivo)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)

        // (opcional) limites simples pra evitar valores absurdos
        etTitulo.filters = arrayOf(InputFilter.LengthFilter(60))

        // ---- prefills ----
        val args = requireArguments()
        val idJogo   = args.getLong(ARG_IDJOGO)
        val titulo   = args.getString(ARG_TITULO).orEmpty()
        val qtd      = args.getInt(ARG_QTD)
        val nivel    = args.getInt(ARG_NIVEL)
        val senha    = args.getString(ARG_SENHA).orEmpty()
        val ativo    = args.getInt(ARG_ATIVO)

        Log.d(TAG, "prefill -> id=$idJogo, titulo=$titulo, qtd=$qtd, nivel=$nivel, ativo=$ativo")

        etTitulo.setText(titulo)
        etNivel.setText(nivel.toString())
        etQtd.setText(qtd.toString())
        etSenha.setText(senha)
        swAtivo.isChecked = (ativo == 1)

        val dialog = MaterialAlertDialogBuilder(ctx)
            .setView(view)
            .create()

        // Abrir teclado focando no título (fica mais fluido)
        dialog.setOnShowListener {
            etTitulo.requestFocus()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }

        // ---- ações ----
        btnCancel.setOnClickListener {
            Log.d(TAG, "btnCancel clicked -> dismiss")
            dialog.dismiss()
        }

        btnUpdate.setOnClickListener {
            val novoTitulo = etTitulo.text?.toString()?.trim().orEmpty()
            val novoNivel  = etNivel.text?.toString()?.toIntOrNull() ?: nivel
            val novaQtd    = etQtd.text?.toString()?.toIntOrNull() ?: qtd
            val novaSenha  = etSenha.text?.toString()?.ifBlank { null }
            val novoAtivo  = if (swAtivo.isChecked) 1 else 0

            Log.d(TAG, "btnUpdate clicked -> result packing...")
            Log.d(
                TAG,
                "out: id=$idJogo, titulo=$novoTitulo, qtd=$novaQtd, nivel=$novoNivel, senha=${novaSenha?.length ?: 0} chars, ativo=$novoAtivo"
            )

            // Repassa também os IDs fixos p/ o Fragment montar o payload
            val result = bundleOf(
                RES_IDJOGO to idJogo,
                RES_TITULO to novoTitulo,
                RES_QTD    to novaQtd,
                RES_NIVEL  to novoNivel,
                RES_SENHA  to novaSenha,
                RES_ATIVO  to novoAtivo,

                ARG_MASTER_ID   to args.getLong(ARG_MASTER_ID),
                ARG_ESPECIFIC   to args.getInt(ARG_ESPECIFIC),
                ARG_TIPO_ID     to args.getLong(ARG_TIPO_ID),
                ARG_GERACAO_ID  to args.getLong(ARG_GERACAO_ID),
                ARG_ESTILO_ID   to args.getLong(ARG_ESTILO_ID),
                ARG_HISTORIA_ID to args.getLong(ARG_HISTORIA_ID),
                ARG_TEMA_ID     to args.getLong(ARG_TEMA_ID)
            )

            setFragmentResult(REQ_KEY, result)
            dialog.dismiss()
        }

        return dialog
    }
}
