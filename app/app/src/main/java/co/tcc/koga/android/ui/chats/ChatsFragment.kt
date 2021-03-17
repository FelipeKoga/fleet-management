package co.tcc.koga.android.ui.chats

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.databinding.ChatsFragmentBinding
import co.tcc.koga.android.utils.Avatar
import co.tcc.koga.android.utils.hide
import co.tcc.koga.android.utils.show

import javax.inject.Inject

class ChatsFragment : Fragment(R.layout.chats_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var adapter: ChatsAdapter
    private lateinit var binding: ChatsFragmentBinding

    private val viewModel by viewModels<ChatsViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatsFragmentBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
        loadUserAvatar()

        viewModel.getAllChats()
        viewModel.observeChatUpdates()
        viewModel.observeUserUpdates()
        viewModel.observeMessageUpdates()
        viewModel.observePushToTalk()

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBarChats.show()
            } else {
                binding.progressBarChats.hide()
            }
        }

        viewModel.chats.observe(viewLifecycleOwner) { chats ->
            adapter.submitList(chats)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error) {
                showErrorToast()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contacts_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_search -> {
//                findNavController().navigate(
//                    R.id.action_chatsFragment_to_searchContactsFragment
//                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun redirectToChat(chat: ChatEntity) {
        findNavController().navigate(
            R.id.action_nav_chatsFragment_to_chatFragment,
            bundleOf("chat" to chat)
        )
    }

    private fun setupViews() {
        binding.toolbarProfile.run {
            inflateMenu(R.menu.contacts_menu)
            setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)
                true
            }
            setOnClickListener {
                findNavController().navigate(
                    R.id.action_chatsFragment_to_profileFragment
                )

            }
        }

        binding.floatingButtonNewChat.setOnClickListener {
            findNavController().navigate(R.id.action_chatsFragment_to_newChatFragment)
        }

    }

    private fun setupRecyclerView() {
        adapter = ChatsAdapter().apply {
            onLoadAvatar = { avatar, isGroup, imageView ->
                Avatar.load(
                    requireContext(),
                    imageView,
                    avatar,
                    if (isGroup) R.drawable.ic_round_group else R.drawable.ic_round_person
                )
            }
            onChatClicked = { chat ->
                redirectToChat(chat)
            }
        }
        binding.recyclerViewChats.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewChats.adapter = adapter
    }


    private fun loadUserAvatar() {
        val avatar = viewModel.getAvatar()
        Avatar.load(
            requireContext(),
            binding.imageViewUserPhoto,
            avatar,
            R.drawable.ic_round_person,
        )
    }

    private fun showErrorToast() {
        Toast.makeText(requireContext(), "Ocorreu um erro ao listar os chats", Toast.LENGTH_LONG)
            .show()
    }
}