package co.tcc.koga.android.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import co.tcc.koga.android.App
import co.tcc.koga.android.R
import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.UserRepository
import co.tcc.koga.android.ui.MainActivity
import com.google.android.gms.location.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class LocationService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var fusedLocationClient: FusedLocationProviderClient? = null

    @Inject
    lateinit var repository: UserRepository

    @Inject
    lateinit var clientRepository: ClientRepository

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.action === Actions.START.name) {
                startService()
            } else {
                stopService()
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        (applicationContext as App).appComponent.mainComponent().create().inject(this)
        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun startService() {
        if (isServiceStarted) return
        isServiceStarted = true
        setLocationServiceState(this, ServiceState.STARTED)
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Location::lock").apply {
                    acquire(10 * 60 * 1000L)
                }
            }
        GlobalScope.launch {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    sendLocation()
                }
                delay(30000)
            }
        }
    }

    private fun stopService() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
        }
        isServiceStarted = false
        setLocationServiceState(this, ServiceState.STOPPED)
    }

    private fun sendLocation() {
        if (!Client.getInstance().isSignIn() && Client.getInstance().currentUser.locationEnabled) return
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
            println("Location: $location")
            if (location != null) {
                repository.sendLocation(location.latitude, location.longitude)
            }
        }


    }

    private fun createNotification(): Notification {
        val notificationChannelId = "LOCATION CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Location channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else Notification.Builder(this)

        return builder
            .setContentTitle("Envio da localização ativo")
            .setContentText(" Clique aqui para gerenciar.")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_location)
            .setOngoing(true)
            .build()
    }
}