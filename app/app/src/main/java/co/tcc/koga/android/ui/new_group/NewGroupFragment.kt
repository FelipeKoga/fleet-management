package co.tcc.koga.android.ui.new_group

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
import co.tcc.koga.android.databinding.NewGroupFragmentBinding
import co.tcc.koga.android.ui.adapter.SelectUserAdapter
import co.tcc.koga.android.ui.adapter.SelectedUserAdapter
import co.tcc.koga.android.utils.hide
import co.tcc.koga.android.utils.show
import javax.inject.Inject


class NewGroupFragment : Fragment() {
    private lateinit var selectAdapter: SelectUserAdapter
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
    ): View? {
        binding = NewGroupFragmentBinding.inflate(inflater)
        setupRecyclerViews()

        viewModel.getUsers()
        viewModel.users.observe(viewLifecycleOwner, {

//            selectAdapter.users = it
            selectAdapter.notifyDataSetChanged()
        })

        viewModel.selectedUsers.observe(viewLifecycleOwner, {
            if (it.isNullOrEmpty()) {
                binding.recyclerViewSelectedUsers.hide()
                binding.floatingButtonCreateGroup.hide()
            } else {
                if (it.isNotEmpty()) binding.floatingButtonCreateGroup.show()
                binding.recyclerViewSelectedUsers.show()
                selectedAdapter.users = it
                selectedAdapter.notifyDataSetChanged()
            }

        })

        viewModel.chatCreated.observe(viewLifecycleOwner, {
            println("Chat created")
            findNavController().popBackStack()
            findNavController().popBackStack()
            findNavController().navigate(
                R.id.action_nav_chatsFragment_to_chatFragment,
                bundleOf("chat" to it)
            )
        })

        binding.floatingButtonCreateGroup.setOnClickListener {
            viewModel.createChat(binding.textFieldGroupName.text.toString(), "")
        }

        return binding.root
    }

    private fun setupRecyclerViews() {
        binding.apply {
//            selectAdapter = SelectUserAdapter(requireContext(), listOf(), fun(user) {
//                viewModel.handleSelectedUser(user)
//            })
            selectedAdapter = SelectedUserAdapter(requireContext(), listOf(), fun(user) {
                viewModel.handleSelectedUser(user)
            })
            recyclerViewUsersNewGroup.layoutManager = LinearLayoutManager(requireContext())
            val layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            recyclerViewUsersNewGroup.adapter = selectAdapter
            recyclerViewSelectedUsers.layoutManager = layoutManager
            recyclerViewSelectedUsers.adapter = selectedAdapter
        }
    }

}