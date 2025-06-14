package com.example.androidapprpg.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding:FragmentSettingsBinding? = null
    private val binding get()= _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false) // Infla o Layout usando ViewBinding
        return  binding.root //retorna a raiz da vieww inflada para ser exibida na tela
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeAccount.setOnClickListener {
            closeAccount()
        }

    }


    //implementar metódo de encerrar a conta
    private fun closeAccount() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_close_account, null)
        val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog = builder.show()

        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirm_button)


        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        //Necessita excluir a conta do banco!
        confirmButton.setOnClickListener {
            Toast.makeText(requireContext(), "Conta Encerrada com sucesso.", Toast.LENGTH_SHORT).show()
            alertDialog.dismiss()
        }
    }

    //evita vazamento de memória
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
