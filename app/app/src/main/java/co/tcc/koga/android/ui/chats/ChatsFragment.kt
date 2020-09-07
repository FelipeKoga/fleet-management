package co.tcc.koga.android.ui.chats

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.AppDatabase
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.ui.login.LoginViewModel
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contacts_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCurrentUser()
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
        viewModel.apply {
            chats.observe(viewLifecycleOwner, { chats ->
                adapter.chats = chats
                adapter.notifyDataSetChanged()
            })

            currentUser.observe(viewLifecycleOwner, { user ->
                if (user?.id !== null) viewModel.getChats()
            })
        }
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
            findNavController().navigate(R.id.action_chatsFragment_to_newGroupFragment)
        }

    }

    private fun setupRecyclerView() {
        adapter = ChatsAdapter(listOf()) { chat ->
            redirectToChat(chat)
        }
        recycler_view_chats.layoutManager = LinearLayoutManager(requireContext())
        recycler_view_chats.adapter = adapter
    }
}