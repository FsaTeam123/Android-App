package com.example.androidapprpg.ui.fragment.GameManager

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentDiceGameBinding
import com.example.androidapprpg.dicecore.DiceSpec
import com.example.androidapprpg.dicecore.RollMode
import com.example.androidapprpg.ui.viewmodel.DicePlayViewModel

class DicePlayFragment : Fragment(R.layout.fragment_dice_game) {

    private val vm: DicePlayViewModel by viewModels()

    private var _binding: FragmentDiceGameBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentDiceGameBinding.bind(view)

        // Configuração "turbo" do DicePhysicsView
        binding.dicePhysicsView.apply {
            sizeFactor = 0.16f
            gravityY = 380.0
            impulseScale = 3.2
            torqueScale  = 2.4
            linearDamping = 0.05
            angularDamping = 0.05
            timeScale = 2.0
            targetFps = 60
            settleLinVel2 = 24.0
            settleAngVel  = 0.9
            settleFrames  = 6
        }

        // 1) Recebe spec via argumentos (opcional)
        val initialSpec: DiceSpec? =
            BundleCompat.getParcelable(requireArguments(), "spec", DiceSpec::class.java)

        initialSpec?.let { spec ->
            Log.d("Dice", "spec (args) = $spec")
            vm.setSpec(spec)
            updateModeChip(spec.rollMode)
            rollNow(spec)
        }

        // 2) Recebe result do diálogo de seleção de dados
        parentFragmentManager.setFragmentResultListener(
            "dice_spec_request",
            viewLifecycleOwner
        ) { _, bundle ->
            val spec = BundleCompat.getParcelable(bundle, "spec", DiceSpec::class.java)
                ?: return@setFragmentResultListener
            Log.d("Dice", "spec (result) = $spec")
            vm.setSpec(spec)
            updateModeChip(spec.rollMode)
            rollNow(spec)
        }

        // 3) Callback do fim da física
        binding.dicePhysicsView.onAllDiceSettled = { result ->
            vm.setResult(result)
            vm.addToHistory(result) // usa postValue no ViewModel
            binding.tvTotal.text = "Total: ${result.total}"
        }

        // 4) Ações UI
        binding.btnOpenDicePanel.setOnClickListener {
            DiceSelectDialog().show(parentFragmentManager, "dice_sheet")
        }
        binding.btnReroll.setOnClickListener {
            vm.lastSpec?.let { rollNow(it.copy(seed = System.nanoTime())) }
        }

        // 5) Restaura UI se voltar
        vm.lastSpec?.let { updateModeChip(it.rollMode) }
        vm.lastResult?.let { binding.tvTotal.text = "Total: ${it.total}" }
    }

    private fun rollNow(spec: DiceSpec) {
        binding.tvTotal.text = "Total: —"
        binding.dicePhysicsView.stop()  // reset
        binding.dicePhysicsView.roll(spec)
    }

    private fun updateModeChip(mode: RollMode) {
        binding.chipMode.text = when (mode) {
            RollMode.NORMAL      -> "Modo: Normal"
            RollMode.VANTAGEM    -> "Modo: Vant."
            RollMode.DESVANTAGEM -> "Modo: Desv."
        }
    }

    override fun onDestroyView() {
        binding.dicePhysicsView.stop()
        _binding = null
        super.onDestroyView()
    }
}
