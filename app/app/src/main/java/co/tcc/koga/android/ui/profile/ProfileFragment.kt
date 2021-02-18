package co.tcc.koga.android.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.ProfileFragmentBinding
import co.tcc.koga.android.ui.MainActivity

import co.tcc.koga.android.utils.loadImage

import javax.inject.Inject

class ProfileFragment : Fragment(R.layout.profile_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: ProfileFragmentBinding
    private val viewModel by viewModels<ProfileViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProfileFragmentBinding.inflate(inflater)
        binding.apply {
            loadUserAvatar()
            toolbarProfile.inflateMenu(R.menu.settings_menu)
            toolbarProfile.setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)
                true
            }
            toolbarProfile.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_settings -> {
                findNavController().navigate(
                    R.id.action_profileFragment_to_settingsFragment
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUserAvatar() {
        val avatar = viewModel.getAvatar()
        loadImage(
            requireContext(),
            binding.imageViewProfilePhoto,
            avatar,
            R.drawable.ic_round_person,
        )
    }
}