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
import co.tcc.koga.android.service.ServiceState
import co.tcc.koga.android.service.getServiceState
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.utils.log
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
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val mLocationRequest = LocationRequest.create()
                        mLocationRequest.interval = 5000
                        mLocationRequest.fastestInterval = 5000
                        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        val mLocationCallback: LocationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                            }
                        }

                        LocationServices.getFusedLocationProviderClient(requireContext())
                            .requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                        actionOnService(Actions.START)
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

    private fun actionOnService(action: Actions) {
        if (getServiceState(requireContext()) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(requireContext(), LocationService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                log("Starting the service in >=26 Mode")
                activity?.startForegroundService(it)
                return
            }
            log("Starting the service in < 26 Mode")
            activity?.startService(it)
        }
    }

    private fun showErrorToast() {
        Toast.makeText(requireContext(), "Erro ao inicializar o app.", Toast.LENGTH_LONG)
            .show()
    }






}