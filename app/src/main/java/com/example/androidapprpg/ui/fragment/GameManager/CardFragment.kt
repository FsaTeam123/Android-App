package com.example.androidapprpg.ui.fragment.GameManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentCardsManagementBinding

class CardFragment : Fragment() {

    private var _binding: FragmentCardsManagementBinding? = null
    private val binding get() = _binding!!

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
        wireGridClicks()
        handleBackPress()

        binding.btnCloseExpanded.setOnClickListener { hideExpanded() }
        binding.expandedOverlay.setOnClickListener { hideExpanded() }
        binding.expandedCard.apply {
            isClickable = true // garante que o clique não “passe” pro overlay
            setOnClickListener { /* consume */ }
        }
    }

    private fun wireGridClicks() = with(binding) {
        for (i in 0 until cardsGridLayout.childCount) {
            val card = cardsGridLayout.getChildAt(i)
            card.isClickable = true
            card.setOnClickListener {
                val title = extractTitleFromCard(card)
                val bgRes = (card.tag as? Int) ?: R.drawable.card_nine
                showExpanded(title, "descrição/efeitos da carta selecionada.", bgRes)
            }
        }
    }

    private fun extractTitleFromCard(view: View): String {
        fun findFirstText(v: View): String? = when (v) {
            is TextView -> v.text?.toString()
            is ViewGroup -> (0 until v.childCount).asSequence()
                .mapNotNull { findFirstText(v.getChildAt(it)) }.firstOrNull()
            else -> null
        }
        return findFirstText(view) ?: "Carta"
    }

    private fun showExpanded(title: String, body: String, bgRes: Int) = with(binding) {
        expandedTitle.text = title
        expandedBody.text  = body
        expandedBg.setImageResource(bgRes)

        expandedOverlay.bringToFront()
        rootScroll.isEnabled = false

        expandedOverlay.alpha = 0f
        expandedOverlay.visibility = View.VISIBLE
        expandedOverlay.animate().alpha(1f).setDuration(160).start()

        expandedCard.scaleX = 0.95f
        expandedCard.scaleY = 0.95f
        expandedCard.animate().scaleX(1f).scaleY(1f).setDuration(220).start()
    }

    private fun hideExpanded() = with(binding) {
        expandedCard.animate().scaleX(0.98f).scaleY(0.98f).setDuration(140).start()
        expandedOverlay.animate().alpha(0f).setDuration(160).withEndAction {
            expandedOverlay.visibility = View.GONE
            rootScroll.isEnabled = true
        }.start()
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.expandedOverlay.visibility == View.VISIBLE) {
                hideExpanded()
            } else {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}