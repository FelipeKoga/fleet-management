package co.tcc.koga.android.ui.chats.new_chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.NewChatFragmentBinding
import co.tcc.koga.android.ui.adapter.UserAdapter
import co.tcc.koga.android.utils.hide
import co.tcc.koga.android.utils.show
import kotlinx.android.synthetic.main.new_chat_fragment.*
import javax.inject.Inject

class NewChatFragment : Fragment(R.layout.new_chat_fragment) {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: NewChatFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<NewChatViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewChatFragmentBinding.inflate(inflater)
        binding.apply {
            toolbarSearchContacts.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            adapter = UserAdapter(container?.context as Context, listOf(), fun(user) {
                progressBarCreateChat.show()
                viewModel.createChat(user.username)
            })
            recyclerViewUsers.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewUsers.adapter = adapter

            floatingButtonNewGroup.setOnClickListener {
                findNavController().navigate(R.id.action_newChatFragment_to_newGroupFragment)
            }

            editTextSearchContact.setOnEditorActionListener { v, _, _ ->
                if (v.text.isNotEmpty()) {
                    viewModel.filterUsers(v.text.toString())
                } else {
                    viewModel.clearFilter()
                }

                true
            }

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.users.observe(viewLifecycleOwner, { users ->
            adapter.users = users
            adapter.notifyDataSetChanged()
        })

        viewModel.filteredUsers.observe(viewLifecycleOwner) { filteredUsers ->
            adapter.users = filteredUsers
            adapter.notifyDataSetChanged()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) binding.progressBarUsers.show() else binding.progressBarUsers.hide()
        }

        viewModel.chatCreated.observe(viewLifecycleOwner, {
            println(it)
            findNavController().popBackStack()
            findNavController().navigate(
                R.id.action_nav_chatsFragment_to_chatFragment,
                bundleOf("chat" to it)
            )
        })

        viewModel.getUsers()

    }

}