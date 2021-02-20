package co.tcc.koga.android.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.LoginFragmentBinding
import co.tcc.koga.android.databinding.SettingsFragmentBinding
import co.tcc.koga.android.service.Actions
import co.tcc.koga.android.service.LocationService
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.utils.UserRole

import javax.inject.Inject


class SettingsFragment : Fragment(R.layout.settings_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<SettingsViewModel> { viewModelFactory }

    private lateinit var binding: SettingsFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            linearLayoutLocationUpdate .visibility =
                if (viewModel.currentUser.role == UserRole.EMPLOYEE.name) View.VISIBLE else View.GONE
            switchLocation.isChecked = viewModel.location
            switchPushToTalk.isChecked = viewModel.pushToTalk
            switchNotification.isChecked = viewModel.notification


            toolbarSettings.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            linearLayoutLogout.setOnClickListener {
                stopLocationService()
                viewModel.signOut()
                findNavController().popBackStack(R.id.loginFragment, true)
                findNavController().navigate(
                    R.id.loginFragment
                )
            }


            switchLocation.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startLocationService()
                } else {
                    stopLocationService()
                }
                viewModel.updateLocation(isChecked)
            }

            switchPushToTalk.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updatePushToTalk(isChecked)
            }

            switchNotification.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateNotification(isChecked)
            }
        }
    }

    private fun stopLocationService() {
        Intent(requireContext(), LocationService::class.java).also {
            it.action = Actions.STOP.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity?.startForegroundService(it)
                return
            }
            activity?.stopService(it)
        }
    }

    private fun startLocationService() {
        Intent(requireContext(), LocationService::class.java).also {
            it.action = Actions.START.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity?.startForegroundService(it)
                return
            }
            activity?.startService(it)
        }
    }
}