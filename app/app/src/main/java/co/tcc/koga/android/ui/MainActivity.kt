package co.tcc.koga.android.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import co.tcc.koga.android.App
import co.tcc.koga.android.R
import co.tcc.koga.android.service.Actions
import co.tcc.koga.android.service.LocationService
import co.tcc.koga.android.service.requestLocationPermission

import co.tcc.koga.android.ui.di.MainComponent
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    lateinit var mainComponent: MainComponent

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        mainComponent = (applicationContext as App).appComponent.mainComponent().create()
        mainComponent.inject(this)

        setTheme(R.style.SplashScreenTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.observeUserStatus()
        viewModel.isSignIn.observe(this) { isSignIn ->
            println("LOGADO?????????????? $isSignIn")
            if (isSignIn) {
                if (viewModel.isLocationEnabled()) {
                    if (requestLocationPermission(this)) {
                        startLocationService()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {

                        startLocationService()
                    }
                }
                return
            }
        }
    }

    private fun startLocationService() {
        Intent(this, LocationService::class.java).also {
            it.action = Actions.START.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
                return
            }
            startService(it)
        }
    }


}