package co.tcc.koga.android.ui.new_chat

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

class NewChatFragment : Fragment() {
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
    ): View? {
        binding = NewChatFragmentBinding.inflate(inflater)
        binding.apply {
            adapter = UserAdapter(requireContext(), listOf(), fun(user) {
                progress_bar_create_chat.show()
                viewModel.createChat(user.username)
            })

            recyclerViewUsers.layoutManager = LinearLayoutManager(container?.context)
            recyclerViewUsers.adapter = adapter

            toolbarSearchContacts.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            floatingButtonNewGroup.setOnClickListener {

                findNavController().navigate(R.id.action_newChatFragment_to_newGroupFragment)
            }

            viewModel.users.observe(viewLifecycleOwner, {
                if (it.isNotEmpty()) {
                    progressBarUsers.hide()
                    recyclerViewUsers.show()
                }
                adapter.users = it
                adapter.notifyDataSetChanged()
            })

//            viewModel.getAllUsers().observe(viewLifecycleOwner, {
//                if (it.status === Resource.Status.ERROR) {
//                    println("Error!")
//                }
//            })

            viewModel.chatCreated.observe(viewLifecycleOwner, {
                findNavController().popBackStack()
                findNavController().navigate(
                    R.id.action_nav_chatsFragment_to_chatFragment,
                    bundleOf("chat" to it)
                )
            })

        }
        return binding.root
    }


}