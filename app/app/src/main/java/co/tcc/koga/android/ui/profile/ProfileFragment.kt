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
            loadProfilePhoto()
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

    private fun loadProfilePhoto() {
        Glide
            .with(this)
            .load("https://scontent.fpgz1-1.fna.fbcdn.net/v/t31.0-8/23509456_1409309355854887_1466911775224434071_o.jpg?_nc_cat=108&_nc_sid=09cbfe&_nc_eui2=AeG623DjsuyKytHojdUetGk7pVh_hLQD8-ulWH-EtAPz68zvBVOrDuTeDTDkcwcMKguceUm-oHC3hNPbdhEQuVZK&_nc_ohc=inhZTXaPe04AX-lM6La&_nc_ht=scontent.fpgz1-1.fna&oh=3c51f2abbda394c3c8165b3abe21e0c3&oe=5F2B1497")
            .centerCrop()
            .apply(RequestOptions.circleCropTransform())
            .error(R.drawable.ic_outline_person)
            .placeholder(R.drawable.ic_round_person)
            .into(binding.imageViewProfilePhoto)
    }
}