package com.example.androidapprpg.ui.fragment.GameManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.androidapprpg.databinding.FragmentCardsManagementBinding
import com.example.androidapprpg.ui.fragment.GameManager.CardsBottomSheetFragment.CardKind
import com.example.androidapprpg.ui.viewmodel.CardsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardFragment : Fragment() {

    private var _binding: FragmentCardsManagementBinding? = null
    private val binding get() = _binding!!

    private val vm: CardsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardsManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // carrega o player uma vez (para Biografia)
        vm.loadPlayer()

        wireGridClicks()
    }

    private fun wireGridClicks() = with(binding) {

        // 1: Biografia
        card1.setOnClickListener {
            CardsBottomSheetFragment
                .newInstance(CardKind.BIOGRAFIA)
                .show(childFragmentManager, "cardBioSheet")
        }

        // 2: Armas
        card2.setOnClickListener {
            CardsBottomSheetFragment
                .newInstance(CardKind.ARMAS)
                .show(childFragmentManager, "cardArmasSheet")
        }

        // 3: Poderes
        card3.setOnClickListener {
            CardsBottomSheetFragment
                .newInstance(CardKind.PODERES)
                .show(childFragmentManager, "cardPoderSheet")
        }

        // 4: Magias
        card4.setOnClickListener {
            CardsBottomSheetFragment
                .newInstance(CardKind.MAGIAS)
                .show(childFragmentManager, "cardMagiaSheet")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

