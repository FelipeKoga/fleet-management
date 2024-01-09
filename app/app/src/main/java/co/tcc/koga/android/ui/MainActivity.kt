package co.tcc.koga.android.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import co.tcc.koga.android.App
import co.tcc.koga.android.R
import co.tcc.koga.android.service.Actions
import co.tcc.koga.android.service.LocationService
import co.tcc.koga.android.service.PushToTalkService
import co.tcc.koga.android.service.requestLocationPermission

import co.tcc.koga.android.ui.di.MainComponent
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

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

        if (intent.extras?.get("notificationId") != null) {
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(intent.getIntExtra("notificationId", 0))
        }

        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1000
        )

        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        viewModel.observeUserStatus()
        viewModel.isSignIn.observe(this) { isSignIn ->
            if (isSignIn) {
                startPushToTalkService()

                FirebaseMessaging.getInstance().token.addOnCompleteListener {
                    viewModel.addNotificationToken(it.result)
                }

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
                    startLocationService()
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