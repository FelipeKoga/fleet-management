package co.tcc.koga.android.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.LocationUpdateLayoutBinding
import co.tcc.koga.android.databinding.SettingsFragmentBinding
import co.tcc.koga.android.ui.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    ): View {
        binding = SettingsFragmentBinding.inflate(inflater)
        binding.apply {
            toolbarSettings.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            linearLayoutLogout.setOnClickListener {
                viewModel.signOut()
                findNavController().navigate(
                    R.id.action_settingsFragment_to_loginFragment
                )
            }

            linearLayoutLocationUpdate.setOnClickListener {
                showLocationOptions()
            }
        }
        return binding.root
    }

    private fun showLocationOptions() {

        val locationView =
            LocationUpdateLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog =  MaterialAlertDialogBuilder(context as Context)
            .setView(locationView.root)
        var builder: androidx.appcompat.app.AlertDialog? = null


        locationView.apply {
            radioGroupLocation.clearCheck()
            var index: Int
            println(viewModel.currentUser.locationUpdate)
            when (viewModel.currentUser.locationUpdate) {
                30 -> {
                    radioButton30Seconds.isChecked = true
                    index = 0
                }
                45 -> {
                    radioButton45Seconds.isChecked = true
                    index = 1
                }
                60 -> {
                    radioButton1Minute.isChecked = true
                    index = 2
                }
                300 -> {
                    radioButton5Minutes.isChecked = true
                    index = 3
                }
                else -> {
                    radioButtonDisabledLocation.isChecked = true
                    index = 4
                }
            }

            println(index)


            radioGroupLocation.checkedRadioButtonId
            radioGroupLocation.setOnCheckedChangeListener { _, checkedId ->
                index = checkedId
            }

            buttonSaveLocationUpdate.setOnClickListener {
                var locationUpdate: Int? = null
                locationUpdate = when(index) {
                    0 -> 30
                    1 -> 45
                    2 -> 60
                    3 -> 300
                    else -> null
                }
                viewModel.updateLocation(locationUpdate)
                builder?.hide()
            }
        }

        builder = dialog.show()
    }

}