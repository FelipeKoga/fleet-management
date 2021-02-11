package co.tcc.koga.android.ui.profile

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.ProfileFragmentBinding
import co.tcc.koga.android.utils.loadImage
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ProfileFragment : Fragment() {
    private lateinit var binding: ProfileFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProfileFragmentBinding.inflate(inflater)
        binding.apply {
//            loadProfilePhoto()
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

//    private fun loadUserAvatar() {
//        val avatar = viewModel.getUserAvatar()
//        loadImage(
//            requireContext(),
//            binding.imageViewUserPhoto,
//            avatar,
//            R.drawable.ic_round_person,
//        )
//    }
}