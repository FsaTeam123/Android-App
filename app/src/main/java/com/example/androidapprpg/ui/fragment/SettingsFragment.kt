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
import androidx.navigation.fragment.findNavController
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentSettings2Binding
import com.example.androidapprpg.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettings2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettings2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeAccount.setOnClickListener {
            closeAccount()
        }
    }

    // Método para exibir o diálogo de encerramento de conta
    private fun closeAccount() {
        // Infla o layout do diálogo corretamente
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_close_account, null)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        // Obtém as referências dos botões dentro da View do diálogo
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirm_button)

        // Configura o clique do botão de cancelar
        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        // Configura o clique do botão de confirmar
        confirmButton.setOnClickListener {
            // Lógica para excluir a conta do banco de dados
            // Por exemplo, chamar uma ViewModel ou um repositório

            // Exemplo de Toast para feedback visual
            Toast.makeText(requireContext(), "Conta Encerrada com sucesso.", Toast.LENGTH_SHORT).show()

            // Fecha o diálogo após a ação
            alertDialog.dismiss()
        }

        binding.btnFechar.setOnClickListener {
            findNavController().navigate(R.id.action_to_home_fragment)
        }


    }

    // Evita vazamento de memória
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}