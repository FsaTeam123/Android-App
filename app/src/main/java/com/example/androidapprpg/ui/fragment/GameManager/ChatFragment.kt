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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidapprpg.R
import com.example.androidapprpg.databinding.FragmentChatBinding
import com.example.androidapprpg.ui.viewmodel.ChatViewModel
import com.example.androidapprpg.utils.websocket.ChatAdapter
import com.example.androidapprpg.utils.websocket.ChatIds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.max

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val vm: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    private lateinit var chatId: String
    private var sendingNow = false

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

        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )

        chatId = buildNewChatId()
        android.util.Log.d("CHAT", ">> chatId resolvido = $chatId")

        setupRecycler()
        setupButtons()
        setupImeActions()
        applyImeInsets()

        // SUBSCRIBE no tópico correto
        vm.subscribe(chatId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.messages.collect { list ->
                    chatAdapter.submitList(list) {
                        if (chatAdapter.itemCount > 0) {
                            binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
                        }
                    }
                }
            }
        }
    }

    /**
     * Regras:
     * - "global"/"mesa": exige idJogo e usa o próprio idJogo como chatId (ex.: "123")
     * - "dm": exige myId e peerUserId -> "dm.<menor>_<maior>"
     * Se idJogo não existir, aborta com erro explícito (evita cair em canais por sessão).
     */
    private fun buildNewChatId(): String {
        val args = arguments
        val tipo = args?.getString("tipoChat")?.lowercase()?.trim()

        fun anyToLongOrNull(a: Any?): Long? = when (a) {
            is Long -> a
            is Int -> a.toLong()
            is String -> a.toLongOrNull()
            else -> null
        }

        val jogoFromArgs = anyToLongOrNull(args?.get("idJogo"))
        val jogoFromIntent = anyToLongOrNull(activity?.intent?.extras?.get("idJogo"))
        val idJogo = jogoFromArgs ?: jogoFromIntent

        val peerFromArgs = anyToLongOrNull(args?.get("peerUserId"))
        val peerFromIntent = anyToLongOrNull(activity?.intent?.extras?.get("peerUserId"))
        val peerUserId = peerFromArgs ?: peerFromIntent

        val myId = vm.currentUserId()

        return when (tipo) {
            "mesa", "global" -> {
                requireNotNull(idJogo) { "idJogo obrigatório para chat de mesa/global" }
                ChatIds.global(idJogo) // => "123"
            }
            "dm" -> {
                require(myId > 0 && peerUserId != null) { "peerUserId e usuário atual obrigatórios para DM" }
                ChatIds.dm(myId, peerUserId!!)
            }
            else -> {
                requireNotNull(idJogo) { "idJogo obrigatório para chat" }
                ChatIds.global(idJogo)
            }
        }
    }

    private fun setupRecycler() = with(binding) {
        chatAdapter = ChatAdapter { vm.currentUserId() }
        recyclerChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
            itemAnimator = null
            adapter = chatAdapter
            setHasFixedSize(true)
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
        if (sendingNow) return@with
        val text = etMessage.text?.toString().orEmpty().trim()
        if (text.isEmpty()) return@with

        sendingNow = true
        btnSend.isEnabled = false

        android.util.Log.d("CHAT", ">> SEND to /app/chat.$chatId.message : $text")
        vm.sendMessage(
            chatId = chatId,
            rawText = text,
            scope = "GLOBAL",
            senderId = vm.currentUserId(),
            senderNick = vm.currentUserNick() ?: "Você"
        )
        etMessage.setText("")

        btnSend.post {
            sendingNow = false
            btnSend.isEnabled = true
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
