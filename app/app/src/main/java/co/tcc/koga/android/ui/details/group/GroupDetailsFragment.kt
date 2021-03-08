package co.tcc.koga.android.ui.details.group

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.GroupDetailsFragmentBinding
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.utils.Avatar
import co.tcc.koga.android.utils.FormatterUtil
import javax.inject.Inject

class GroupDetailsFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: GroupDetailsFragmentBinding
    private val args: GroupDetailsFragmentArgs by navArgs()

    private val viewModel by viewModels<GroupDetailsViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GroupDetailsFragmentBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            setupGroupDetails()
            toolbarChatDetails.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            recyclerViewGroupMembers.layoutManager = LinearLayoutManager(requireContext())
            val adapter = GroupMembersAdapter(args.chat, viewModel.user.username).apply {
                onLoadAvatar = { avatar, imageView ->
                    Avatar.load(
                        requireContext(),
                        imageView,
                        avatar,
                        R.drawable.ic_round_person
                    )
                }

                onMemberClicked = { member ->
                    findNavController().navigate(
                        R.id.action_groupDetailsFragment_to_userDetailsFragment,
                        bundleOf("user" to member)
                    )
                }
            }

            recyclerViewGroupMembers.adapter = adapter

            adapter.submitList(args.chat.members)
        }
    }

    private fun setupGroupDetails() {
        binding.run {
            Avatar.load(
                requireContext(),
                imageViewChatDetailAvatar,
                args.chat.avatar,
                R.drawable.ic_round_group
            )

            toolbarChatDetails.title = args.chat.groupName
            textViewGroupCreatedAt.text = FormatterUtil.getFullDate(args.chat.createdAt as Long)
            textViewGroupAdmin.text =
                args.chat.members?.find { it.username == viewModel.user.username }?.name
        }
    }

}