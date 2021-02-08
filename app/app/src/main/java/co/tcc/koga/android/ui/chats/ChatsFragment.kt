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
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.databinding.ChatsFragmentBinding
import co.tcc.koga.android.utils.loadImage
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
        println("ON CREATE VIEW ===================")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
        setupObservers()
        loadUserAvatar()

        viewModel.getAllChats()
    }

    override fun onResume() {
        super.onResume()
        println("ON RESUME  ===================")
        viewModel.chats.observe(viewLifecycleOwner, { chats ->
            println("=== UPDATE LIST CHATS")
            println(chats)
            adapter.load(chats)
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
        viewModel.chats.observe(viewLifecycleOwner, { chats ->
            adapter.load(chats)
        })
        viewModel.observeChatUpdates()
        viewModel.observeUserUpdates()
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
        adapter = ChatsAdapter({ avatar, isGroup, imageView ->
            loadImage(
                requireContext(),
                imageView,
                avatar,
                if (isGroup) R.drawable.ic_round_group else R.drawable.ic_round_person
            )
        }, { chat ->
            redirectToChat(chat)
        })

        binding.recyclerViewChats.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewChats.adapter = adapter
    }


    private fun loadUserAvatar() {
        val avatar = viewModel.getUserAvatar()
        loadImage(
            requireContext(),
            binding.imageViewUserPhoto,
            avatar,
            R.drawable.ic_round_person,
        )

    }
}