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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.databinding.GroupDetailsFragmentBinding
import co.tcc.koga.android.ui.adapter.ChatAdapter
import co.tcc.koga.android.ui.adapter.UserAdapter
import co.tcc.koga.android.utils.hide
import co.tcc.koga.android.utils.hideKeyboard
import co.tcc.koga.android.utils.show
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.chat_fragment.*
import java.io.IOException
import javax.inject.Inject

class ChatFragment : Fragment(R.layout.chat_fragment) {
    private lateinit var adapter: ChatAdapter
    private val args: ChatFragmentArgs by navArgs()
    private var recorder: MediaRecorder? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ChatViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.openChat(args.chat.chatId)
        setupToolbar()
        setupViews()
        setupRecyclerView()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        setupObservers()
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

    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                adapter.messages = it
                adapter.notifyDataSetChanged()
                if (adapter.messages.size > 2) {
                    recycler_view_chat_messages.scrollToPosition(adapter.messages.size - 1)
                }
            }
        })
        viewModel.getMessages(args.chat.chatId).observe(viewLifecycleOwner, {
            println(it)
            if (it.status === Resource.Status.LOADING && adapter.messages.isEmpty()) {
                progress_bar_loading_messages.show()
            } else {
                progress_bar_loading_messages.hide()
            }
        })

        viewModel.messageReceived().observe(viewLifecycleOwner) { message ->
            viewModel.openChat(args.chat.chatId)
            viewModel.handleNewMessage(args.chat.chatId, message, false)
        }

        viewModel.messageSent().observe(viewLifecycleOwner) { message ->
            viewModel.handleNewMessage(args.chat.chatId, message, true)
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
        adapter = ChatAdapter(listOf(), Client.getInstance().username(), args.chat.members)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chat_messages.layoutManager = linearLayoutManager
        recycler_view_chat_messages.adapter = adapter
    }

    private fun setupToolbar() {
        text_view_chat_title.text =
            if (args.chat.isPrivate) args.chat.user?.fullName else args.chat.groupName
        loadChatAvatar(image_view_chat_avatar)
        toolbar_chat.apply {
            inflateMenu(R.menu.chat_menu)
            setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)
                true
            }
            setNavigationOnClickListener {
                findNavController().popBackStack()
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
                if (args.chat.isPrivate) args.chat.user?.avatar
                else args.chat.avatar
            )
            .centerInside()
            .apply(RequestOptions.circleCropTransform())
            .error(if (args.chat.isPrivate) R.drawable.ic_round_person else R.drawable.ic_round_group)
            .placeholder(if (args.chat.isPrivate) R.drawable.ic_round_person else R.drawable.ic_round_group)
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
            recycler_view_chat_messages.scrollToPosition(adapter.messages.size - 1)
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
                    args.chat.chatId
                )
            }
        }
    }

    private fun openChatDetails() {
        if (args.chat.isPrivate) {

        } else {
            val contactDetailsView =
                GroupDetailsFragmentBinding.inflate(LayoutInflater.from(requireContext()))
            contactDetailsView.recyclerViewGroupMembers.layoutManager = LinearLayoutManager(context)
            contactDetailsView.recyclerViewGroupMembers.adapter =
                UserAdapter(requireContext(), args.chat.members!!, fun(_) {})
            MaterialAlertDialogBuilder(context as Context)
                .setView(contactDetailsView.root)
                .show()
        }


    }
}

