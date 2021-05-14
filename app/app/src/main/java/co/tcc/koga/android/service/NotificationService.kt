package co.tcc.koga.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.tcc.koga.android.R
import co.tcc.koga.android.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.ThreadLocalRandom

class NotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("ON NEW TOKEN: ${token}")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_id)
            val descriptionText = getString(R.string.notification_id)
            val channel = NotificationChannel(
                getString(R.string.notification_id),
                name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = descriptionText
                lightColor = Color.BLUE
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        println("RECEIVE NOTIFICATION!!!===========================")
        println(remoteMessage)
        val notificationId = ThreadLocalRandom.current().nextInt()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        intent.putExtra("chatId", remoteMessage.data["id"])
        intent.putExtra("notificationId", notificationId)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                this,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        createNotificationChannel()
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val builder =
            NotificationCompat.Builder(this, getString(R.string.notification_id))
                .setColor(0xFF5399DB.toInt())
                .setColorized(true)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setLights(Color.BLUE, 1000, 1000)
                .setPriority(NotificationManagerCompat.IMPORTANCE_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_baseline_message)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(body)

        with(NotificationManagerCompat.from(this)) {
            notify(ThreadLocalRandom.current().nextInt(), builder.build())
        }
    }

}