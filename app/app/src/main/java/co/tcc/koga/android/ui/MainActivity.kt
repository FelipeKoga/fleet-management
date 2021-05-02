package co.tcc.koga.android.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import co.tcc.koga.android.App
import co.tcc.koga.android.R
import co.tcc.koga.android.service.Actions
import co.tcc.koga.android.service.LocationService
import co.tcc.koga.android.service.PushToTalkService
import co.tcc.koga.android.service.requestLocationPermission

import co.tcc.koga.android.ui.di.MainComponent
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    lateinit var mainComponent: MainComponent

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }

    private var alreadyStarted: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        mainComponent = (applicationContext as App).appComponent.mainComponent().create()
        mainComponent.inject(this)

        setTheme(R.style.SplashScreenTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1000
        )

        viewModel.observeUserStatus()
        viewModel.isSignIn.observe(this) { isSignIn ->
            if (isSignIn) {
                startPushToTalkService()
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
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED && viewModel.isSignIn() && viewModel.isLocationEnabled()
                ) {
                    println("TESTEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE222222")
//                    startLocationService()
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

    private fun startPushToTalkService() {
        if (alreadyStarted) return
        alreadyStarted = true
        Intent(this, PushToTalkService::class.java).also {
            it.action = Actions.START.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
                return
            }
            startService(it)
        }
    }
}