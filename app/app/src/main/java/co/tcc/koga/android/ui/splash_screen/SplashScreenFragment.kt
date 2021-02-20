package co.tcc.koga.android.ui.splash_screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.R
import co.tcc.koga.android.service.Actions
import co.tcc.koga.android.service.LocationService
import co.tcc.koga.android.service.requestLocationPermission
import co.tcc.koga.android.ui.MainActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import javax.inject.Inject


class SplashScreenFragment : Fragment(R.layout.splash_screen_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<SplashScreenViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onStart() {
        super.onStart()
        viewModel.initApp()
        viewModel.uiState.asLiveData().observe(viewLifecycleOwner) { state ->
            when (state) {
                SplashScreenUiState.LoggedIn -> {
                    if (viewModel.isLocationEnabled() && requestLocationPermission(requireContext())) {
                        startService()
                    }

                    findNavController().navigate(
                        R.id.action_splashScreenFragment_to_chatsFragment,
                    )
                }
                SplashScreenUiState.LoggedOut -> findNavController().navigate(
                    R.id.action_splashScreenFragment_to_loginFragment,
                )
                SplashScreenUiState.Error -> showErrorToast()
                else -> Log.e("Splashscreen", "inicializando o app...")
            }

        }
    }

    private fun startService() {
        Intent(requireContext(), LocationService::class.java).also {
            it.action = Actions.START.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity?.startForegroundService(it)
                return
            }
            activity?.startService(it)
        }
    }

    private fun showErrorToast() {
        Toast.makeText(requireContext(), "Erro ao inicializar o app.", Toast.LENGTH_LONG)
            .show()
    }


}