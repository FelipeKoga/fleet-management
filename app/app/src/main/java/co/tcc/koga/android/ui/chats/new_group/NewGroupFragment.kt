package co.tcc.koga.android.ui.chats.new_group

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.NewGroupFragmentBinding
import co.tcc.koga.android.ui.adapter.SelectedUserAdapter
import co.tcc.koga.android.ui.adapter.UserAdapter
import co.tcc.koga.android.utils.Avatar
import co.tcc.koga.android.utils.hide
import co.tcc.koga.android.utils.show
import javax.inject.Inject


class NewGroupFragment : Fragment() {
    private lateinit var selectAdapter: UserAdapter
    private lateinit var selectedAdapter: SelectedUserAdapter
    private lateinit var binding: NewGroupFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<NewGroupViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NewGroupFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupViewModel()

        binding.floatingButtonCreateGroup.setOnClickListener {
            viewModel.createChat(binding.textFieldGroupName.text.toString(), "")
        }
    }

    private fun setupRecyclerViews() {
        binding.apply {
            selectAdapter = UserAdapter().apply {
                onLoadAvatar = { avatar, imageView ->
                    loadAvatar(avatar, imageView)
                }


                onUserClicked = { user ->
                    viewModel.handleUserSelected(user)
                }
            }


            selectedAdapter = SelectedUserAdapter().apply {
                onUserClicked = { user ->
                    viewModel.handleUserSelected(user)
                }

                onLoadAvatar = { avatar, imageView ->
                    loadAvatar(avatar, imageView)
                }
            }

            recyclerViewUsersNewGroup.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewUsersNewGroup.adapter = selectAdapter

            val layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            recyclerViewSelectedUsers.layoutManager = layoutManager
            recyclerViewSelectedUsers.adapter = selectedAdapter
        }
    }

    private fun setupViewModel() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            selectAdapter.submitList(users)
        }

        viewModel.selectedUsers.observe(viewLifecycleOwner) {
            binding.run {
                if (it.isNullOrEmpty()) {
                    recyclerViewSelectedUsers.hide()
                    floatingButtonCreateGroup.hide()
                } else {
                    if (it.isNotEmpty()) floatingButtonCreateGroup.show()
                    recyclerViewSelectedUsers.show()
                }
            }

            selectedAdapter.submitList(it)
        }

        viewModel.chatCreated.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
            findNavController().popBackStack()
            findNavController().navigate(
                R.id.action_nav_chatsFragment_to_chatFragment,
                bundleOf("chat" to it)
            )
        }

        viewModel.getUsers()
    }


    private fun loadAvatar(avatar: String?, imageView: ImageView) {
        Avatar.load(
            requireContext(),
            imageView,
            avatar,
            R.drawable.ic_round_person
        )
    }
}