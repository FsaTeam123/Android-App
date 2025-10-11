package com.example.androidapprpg.ui.fragment.GameManager

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidapprpg.R
import com.example.androidapprpg.adapter.ChatAdapter
import com.example.androidapprpg.databinding.FragmentChatBinding
import com.example.androidapprpg.ui.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val vm: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setupButtons()
        setupImeActions()
        applyImeInsets()

        // pegue o chatId da navegação/args (ajuste para SafeArgs se já tiver)
        val chatId = arguments?.getString("chatId") ?: "default"
        vm.connect(chatId)

        // observa mensagens e faz autoscroll
        vm.messages.observe(viewLifecycleOwner) { list ->
            chatAdapter.submitList(list) {
                if (chatAdapter.itemCount > 0) {
                    binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
    }

    private fun setupRecycler() = with(binding) {
        chatAdapter = ChatAdapter { vm.meId }
        recyclerChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
            itemAnimator = null
            adapter = chatAdapter
        }
    }

    private fun setupButtons() = with(binding) {
        btnSend.setOnClickListener { sendCurrentText() }
        btnBackChat.setOnClickListener {
            val popped = findNavController().popBackStack(R.id.gameManager, false)
            if (!popped) findNavController().navigateUp()
        }
    }

    private fun setupImeActions() = with(binding) {
        etMessage.setOnEditorActionListener { _, actionId, event ->
            val pressedEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (actionId == EditorInfo.IME_ACTION_SEND || pressedEnter) {
                sendCurrentText()
                true
            } else false
        }
    }

    private fun sendCurrentText() = with(binding) {
        val text = etMessage.text?.toString().orEmpty().trim()
        if (text.isNotEmpty()) {
            vm.send(text)
            etMessage.setText("")
        }
    }

    private fun applyImeInsets() = with(binding) {
        ViewCompat.setOnApplyWindowInsetsListener(chatRoot) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottom = max(ime.bottom, sys.bottom)

            chatInputLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = bottom
            }
            recyclerChat.updatePadding(
                left = recyclerChat.paddingLeft,
                top = max(recyclerChat.paddingTop, sys.top),
                right = recyclerChat.paddingRight,
                bottom = max(recyclerChat.paddingBottom, bottom + 8)
            )
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
