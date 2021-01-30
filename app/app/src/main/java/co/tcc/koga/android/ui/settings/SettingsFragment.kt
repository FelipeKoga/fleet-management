
package co.tcc.koga.android.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.SettingsFragmentBinding
import co.tcc.koga.android.ui.chats.ChatsViewModel
import javax.inject.Inject

class SettingsFragment : Fragment() {

    private lateinit var binding: SettingsFragmentBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsFragmentBinding.inflate(inflater)
        binding.apply {
            toolbarSettings.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            linearLayoutLogout.setOnClickListener {
                viewModel.signOut()
                findNavController().navigate(
                    R.id.action_settingsFragment_to_loginFragment,
                )            }
        }
        return binding.root
    }

}