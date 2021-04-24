package co.tcc.koga.android.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import co.tcc.koga.android.App
import co.tcc.koga.android.R
import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.network.payload.RecevingPTT
import co.tcc.koga.android.data.network.socket.PushToTalkActions
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.PushToTalkRepository
import co.tcc.koga.android.data.repository.UserRepository
import co.tcc.koga.android.ui.MainActivity
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class PushToTalkService : Service() {
    private var isServiceStarted = false
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    @Inject
    lateinit var repository: PushToTalkRepository

    private val aa = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private val format = AudioFormat.Builder()
        .setSampleRate(8000)
        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

    private val audioTrack = AudioTrack(aa, format, 1024, AudioTrack.MODE_STREAM, 0)

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
        startForeground(2, createPTTNotification())
    }

    private fun startService() {
        setPTTSerivceState(this, ServiceState.STARTED)
        observePushToTalk()
    }

    private fun observePushToTalk() {
        var hasStarted = false
        compositeDisposable.add(repository.receive().subscribe {
            if (it.action == PushToTalkActions.STARTED_PUSH_TO_TALK) {
                repository.setReceivingPTT(RecevingPTT(true, it.body.user))
            }
            if (it.action == PushToTalkActions.STOPPED_PUSH_TO_TALK) {
                repository.setReceivingPTT(RecevingPTT(false, null))
            }

            if (it.action == PushToTalkActions.RECEIVED_PUSH_TO_TALK) {
                if (it.body.inputData != null) {
                    val doubleArray = it.body.inputData.split(',')
                        .map { floatString -> floatString.toFloat() }
                    val float = doubleArray.toFloatArray()
                    audioTrack.write(float, 0, 1024, AudioTrack.WRITE_BLOCKING)
                }
                if (!hasStarted) {
                    audioTrack.play()
                    hasStarted = true
                }
            }
        })


    }

    private fun stopService() {
        try {
            compositeDisposable.dispose()
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
        }
        isServiceStarted = false
        setPTTSerivceState(this, ServiceState.STOPPED)
    }

    private fun createPTTNotification(): Notification {
        val notificationChannelId = "PTT CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "PTT channel 2",
                NotificationManager.IMPORTANCE_MIN
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
            .setContentTitle("Push to talk")
            .setContentText("Habiltiado para receber mensagens por Push To Talk")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_round_speaker_phone)
            .setOngoing(true)
            .build()
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "PUSH TO TALK CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "PTT channel",
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
            .setContentTitle("Koga está falando...")
            .setContentText(" Clique aqui para abrir o chat.")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_round_speaker_phone)
            .setOngoing(true)
            .build()
    }
}