package co.tcc.koga.android.ui.chats.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.ChatFragmentBinding
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.utils.*
import java.io.*
import javax.inject.Inject


class ChatFragment : Fragment(R.layout.chat_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ChatFragmentBinding
    private val args: ChatFragmentArgs by navArgs()
    private val viewModel by viewModels<ChatViewModel> { viewModelFactory }

    private lateinit var adapter: MessageAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatFragmentBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupViews()
        setupRecyclerView()
        viewModelObservers()
    }

    private fun viewModelObservers() {
        viewModel.run {
            chatId = args.chat.id
            openChat()
            getMessages()
            observeMessageUpdates()
            observeChatUpdates(args.chat)
            observeUserUpdates()

            messages.observe(viewLifecycleOwner) { messages ->
                adapter.submitList(messages)
                binding.recyclerViewChatMessages.scrollToPosition(messages.size - 1)
            }

            chat.observe(viewLifecycleOwner) { chat ->
                binding.run {
                    loadChatAvatar(imageViewChatAvatar)
                    if (chat.user !== null) {
                        textViewChatTitle.text = chat.user?.name
                        imageViewUserStatusOnline.hide()
                        imageViewUserStatusOffline.hide()
                        if (chat.user?.status == "ONLINE") {
                            imageViewUserStatusOnline.show()
                        } else {
                            imageViewUserStatusOffline.show()
                        }
                    } else {
                        textViewChatTitle.text = chat.groupName
                    }
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_speaker -> {
                binding.run {
                    linearLayoutChatSpeaker.show()
                    linearLayoutChatKeyboard.hide()
                    toolbarChat.menu.findItem(R.id.nav_keyboard).isVisible = true
                }
                item.isVisible = false
                true
            }
            R.id.nav_keyboard -> {
                binding.run {
                    linearLayoutChatSpeaker.hide()
                    linearLayoutChatKeyboard.show()
                    toolbarChat.menu.findItem(R.id.nav_speaker).isVisible = true
                }
                item.isVisible = false
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun setupRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true
        adapter = MessageAdapter(viewModel.username, args.chat.members, requireContext())
        binding.recyclerViewChatMessages.layoutManager = linearLayoutManager
        binding.recyclerViewChatMessages.adapter = adapter
        binding.recyclerViewChatMessages.viewTreeObserver.addOnGlobalLayoutListener { scrollToEnd() }
    }

    private fun scrollToEnd() =
        (adapter.itemCount - 1).takeIf { it > 0 }
            ?.let(binding.recyclerViewChatMessages::smoothScrollToPosition)

    private fun setupToolbar() {
        binding.run {
            loadChatAvatar(imageViewChatAvatar)
            textViewChatTitle.text =
                if (args.chat.user !== null) args.chat.user?.name else args.chat.groupName
            toolbarChat.apply {
                inflateMenu(R.menu.chat_menu)
                setOnMenuItemClickListener { item ->
                    onOptionsItemSelected(item)
                    true
                }
                setNavigationOnClickListener {
                    if (findNavController().popBackStack(R.id.chatsFragment, false)) {
                    } else {
                        findNavController().navigate(R.id.chatsFragment)
                    }
                }
                setOnClickListener {
                    openChatDetails()
                }
            }
        }

    }

    private fun loadChatAvatar(imageView: ImageView) {
        val url: String? = if (args.chat.user !== null) {
            val user = args.chat.user as UserEntity
            user.avatar ?: Constants.getAvatarURL(user.name,user.color)
        } else {
            args.chat.avatar
        }

        Avatar.load(
            requireContext(),
            imageView,
            url,
            if (args.chat.user !== null) R.drawable.ic_round_person else R.drawable.ic_round_group
        )
    }

    private fun hideKeyboard() {
        val parentActivity = requireActivity()
        if (parentActivity is AppCompatActivity) {
            parentActivity.hideKeyboard()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupViews() {
        binding.run {
            editTextMessage.addTextChangedListener {
                recyclerViewChatMessages.scrollToPosition(adapter.itemCount - 1)
                if (it != null) {
                    if (it.isNotEmpty()) {
                        imageButtonSendMessage.show()
                    } else {
                        imageButtonSendMessage.hide()
                    }
                }
            }

            imageButtonSendAudio.setOnLongClickListener {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    val permissions = arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    requestPermissions(permissions, 0)
                } else {
                    startRecording()
                }
                true
            }

            imageButtonSendAudio.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        stopRecording()
                        return@setOnTouchListener true
                    }
                }
                false
            }

            imageButtonSendMessage.setOnClickListener {
                val message = editTextMessage.text.toString()
                if (message.isNotEmpty()) {
                    editTextMessage.text.clear()
                    sendMessage(message)
                }
            }
        }

    }

    private fun startRecording() {
        viewModel.startRecording()
    }

    private fun stopRecording() {
        viewModel.stopRecording()
    }

    private fun sendMessage(message: String) {
        viewModel.sendMessage(message, args.chat.id)
    }

    private fun openChatDetails() {
        if (args.chat.user !== null) {
            findNavController().navigate(
                R.id.action_chatFragment_to_userDetailsFragment,
                bundleOf("user" to args.chat.user as UserEntity)
            )
        } else {
            findNavController().navigate(
                R.id.action_chatFragment_to_groupDetailsFragment,
                bundleOf("chat" to args.chat)
            )
        }

    }
}

