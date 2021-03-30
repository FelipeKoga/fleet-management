package co.tcc.koga.android.ui.details.user

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.UserDetailsFragmentBinding
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.utils.Avatar
import co.tcc.koga.android.utils.Constants
import javax.inject.Inject

class UserDetailsFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: UserDetailsFragmentBinding
    private val args: UserDetailsFragmentArgs by navArgs()

    private val viewModel by viewModels<UserDetailsViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UserDetailsFragmentBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            toolbarChatDetails.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            setupUserDetails()
        }
    }


    private fun setupUserDetails() {
        binding.run {
            Avatar.load(
                requireContext(),
                imageViewChatDetailAvatar,
                if (args.user.avatar.isNullOrEmpty()) Constants.getAvatarURL(
                    args.user.name,
                    args.user.color,
                    120
                ) else args.user.avatar as String,

                R.drawable.ic_round_person
            )
            textViewUserName.text = args.user.name
            textViewCustomName.text = args.user.customName ?: "-"
            textViewEmail.text = args.user.email
            textViewPhone.text = args.user.phone
            textViewRole.text = viewModel.getRole(args.user)
        }
    }
}