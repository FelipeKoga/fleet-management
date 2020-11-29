package co.tcc.koga.android.ui.chats

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.ui.adapter.ChatsAdapter
import co.tcc.koga.android.utils.hide
import co.tcc.koga.android.utils.show
import kotlinx.android.synthetic.main.chats_fragment.*
import javax.inject.Inject

class ChatsFragment : Fragment(R.layout.chats_fragment) {
    private lateinit var adapter: ChatsAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ChatsViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllChats().observe(viewLifecycleOwner, {
            if (it.status === Resource.Status.LOADING) {
                if (!it.data.isNullOrEmpty()) progress_bar_chats.hide()
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contacts_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_search -> {
                findNavController().navigate(
                    R.id.action_chatsFragment_to_searchContactsFragment
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupObservers() {


        viewModel.chats.observe(viewLifecycleOwner, {
            if (!it.isNullOrEmpty()) {
                recycler_view_chats.show()
                progress_bar_chats.hide()
                adapter.chats = it
                adapter.notifyDataSetChanged()

            }
        })

        viewModel.messageReceived().observe(viewLifecycleOwner, {
            viewModel.handleNewMessage(it, true)
        })

        viewModel.messageSent().observe(viewLifecycleOwner, {
            viewModel.handleNewMessage(it, false)
        })

    }

    private fun redirectToChat(chat: ChatEntity) {
        findNavController().navigate(
            R.id.action_nav_chatsFragment_to_chatFragment,
            bundleOf("chat" to chat)
        )
    }

    private fun setupViews() {
        toolbar_profile.inflateMenu(R.menu.contacts_menu)
        toolbar_profile.setOnMenuItemClickListener { item ->
            onOptionsItemSelected(item)
            true
        }
        toolbar_profile_content.setOnClickListener {
            findNavController().navigate(
                R.id.action_chatsFragment_to_profileFragment
            )

        }
        floatingButtonNewChat.setOnClickListener {
            findNavController().navigate(R.id.action_chatsFragment_to_newChatFragment)
        }

    }

    private fun setupRecyclerView() {
        adapter = ChatsAdapter(requireContext(), listOf()) { chat ->
            redirectToChat(chat)
        }
        recycler_view_chats.layoutManager = LinearLayoutManager(requireContext())
        recycler_view_chats.adapter = adapter
    }
}