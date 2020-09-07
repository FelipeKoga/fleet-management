package co.tcc.koga.android.ui.chat

import android.content.Context
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
import co.tcc.koga.android.utils.hide
import co.tcc.koga.android.utils.hideKeyboard
import co.tcc.koga.android.utils.show
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.chat_fragment.*
import javax.inject.Inject

class ChatFragment : Fragment(R.layout.chat_fragment) {
    private lateinit var adapter: ChatAdapter
    private val args: ChatFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ChatViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChatAdapter(listOf())
        setupToolbar()
        setupViews()
        setupObservers()
        viewModel.getMessages(args.chat.id)
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
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.messages = messages
            adapter.notifyDataSetChanged()
            recycler_view_chat_messages.scrollToPosition(adapter.messages.size - 1)
        }

        viewModel.loadingMessages.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) progress_bar_loading_messages.show()
            else progress_bar_loading_messages.hide()
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            adapter = ChatAdapter(listOf(), user.id)
            setupRecyclerView()
        }

        viewModel.receiveNewMessage().observe(viewLifecycleOwner) { message ->
            viewModel.handleNewMessage(message, false)
        }

        viewModel.messageSent().observe(viewLifecycleOwner) { message ->
            viewModel.handleNewMessage(message, true)
        }
    }

    private fun setupRecyclerView() {
        recycler_view_chat_messages.layoutManager = LinearLayoutManager(context)
        recycler_view_chat_messages.adapter = adapter
        recycler_view_chat_messages.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (!adapter.messages.isNullOrEmpty()) {
                recycler_view_chat_messages.smoothScrollToPosition(
                    adapter.messages.size
                )
            }
        }
    }

    private fun setupToolbar() {
        text_view_chat_user.text = args.chat.user?.name
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
//                val contactDetailsBinding = ContactDetailsFragmentBinding.inflate(inflater)
//                loadContactPhoto(contactDetailsBinding.imageViewContactDetailsPhoto)
//                MaterialAlertDialogBuilder(context as Context)
//                    .setView(contactDetailsBinding.root)
//                    .show()
            }
        }
    }

    private fun loadContactPhoto(imageView: ImageView) {
        Glide
            .with(requireContext())
            .load("https://www.osprofanos.com/wp-content/uploads/2013/03/cat37.jpg")
            .centerCrop()
            .apply(RequestOptions.circleCropTransform())
            .error(R.drawable.ic_outline_person)
            .placeholder(R.drawable.ic_round_person)
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
                    linear_layout_keyboard_action.hide()
                    image_button_send_message.show()
                } else {
                    linear_layout_keyboard_action.show()
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
                    args.chat.user?.id as String,
                    args.chat.id
                )
            }
        }
    }
}