package co.tcc.koga.android.ui.chat

import android.content.Context
import android.media.MediaRecorder
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.ChatFragmentBinding
import co.tcc.koga.android.utils.hide
import co.tcc.koga.android.utils.hideKeyboard
import co.tcc.koga.android.utils.show
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.chat_fragment.*
import java.io.IOException
import javax.inject.Inject

class ChatFragment : Fragment(R.layout.chat_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ChatFragmentBinding
    private val args: ChatFragmentArgs by navArgs()
    private val viewModel by viewModels<ChatViewModel> { viewModelFactory }
    private var recorder: MediaRecorder? = null
    private lateinit var adapter: ChatAdapter

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
        viewModel.openChat(args.chat.id)
        viewModel.getMessages(args.chat.id)
    }

    override fun onStart() {
        super.onStart()
        viewModel.run {
            observeMessageUpdates(args.chat.id)
            observeChatUpdates(args.chat)
            observeUserUpdates()

            adapter.load(args.chat.messages)
            messages.observe(viewLifecycleOwner) { messages ->
                adapter.load(messages)
                if (adapter.itemCount > 2) {
                    binding.recyclerViewChatMessages.scrollToPosition(adapter.itemCount - 1)
                }
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
                linear_layout_chat_speaker.show()
                linear_layout_chat_keyboard.hide()
                item.isVisible = false
                toolbar_chat.menu.findItem(R.id.nav_keyboard).isVisible = true
                true
            }
            R.id.nav_keyboard -> {
                linear_layout_chat_speaker.hide()
                linear_layout_chat_keyboard.show()
                item.isVisible = false
                toolbar_chat.menu.findItem(R.id.nav_speaker).isVisible = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile("audio ${Math.random()}")
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                println("prepare() failed")
            }

            start()
        }
    }

    private fun setupRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true
        adapter = ChatAdapter(viewModel.username, args.chat.members)
        binding.recyclerViewChatMessages.layoutManager = linearLayoutManager
        binding.recyclerViewChatMessages.adapter = adapter
    }

    private fun setupToolbar() {
        text_view_chat_title.text =
            if (args.chat.user !== null) args.chat.user?.name else args.chat.groupName
        loadChatAvatar(image_view_chat_avatar)
        toolbar_chat.apply {
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
//                findNavController().popBackStack()
            }
            setOnClickListener {
                openChatDetails()
            }
        }
    }

    private fun loadChatAvatar(imageView: ImageView) {
        Glide
            .with(requireContext())
            .load(
                if (args.chat.user !== null) args.chat.user?.avatarUrl
                else args.chat.avatar
            )
            .centerInside()
            .apply(RequestOptions.circleCropTransform())
            .error(if (args.chat.user !== null) R.drawable.ic_round_person else R.drawable.ic_round_group)
            .placeholder(if (args.chat.user !== null) R.drawable.ic_round_person else R.drawable.ic_round_group)
            .into(imageView)
    }

    private fun hideKeyboard() {
        val parentActivity = requireActivity()
        if (parentActivity is AppCompatActivity) {
            parentActivity.hideKeyboard()
        }
    }

    private fun setupViews() {
        edit_text_message.addTextChangedListener {
            recycler_view_chat_messages.scrollToPosition(adapter.itemCount - 1)
            if (it != null) {
                if (it.isNotEmpty()) {
                    image_button_send_message.show()
                } else {
                    image_button_send_message.hide()
                }
            }
        }
        image_button_send_message.setOnClickListener {
            val message = edit_text_message.text.toString()
            if (message.isNotEmpty()) {
                edit_text_message.text.clear()
                viewModel.sendMessage(
                    message,
                    args.chat.id
                )
            }
        }
    }

    private fun openChatDetails() {
//        if (args.chat.isPrivate) {
//
//        } else {
//            val contactDetailsView =
//                GroupDetailsFragmentBinding.inflate(LayoutInflater.from(requireContext()))
//            contactDetailsView.recyclerViewGroupMembers.layoutManager = LinearLayoutManager(context)
//            contactDetailsView.recyclerViewGroupMembers.adapter =
//                UserAdapter(requireContext(), args.chat.members!!, fun(_) {})
//            MaterialAlertDialogBuilder(context as Context)
//                .setView(contactDetailsView.root)
//                .show()
//        }


    }
}

