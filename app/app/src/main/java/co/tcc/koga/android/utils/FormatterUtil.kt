package co.tcc.koga.android.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FormatterUtil {
    companion object {
        fun getHour(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
            val netDate = Date(timestamp)

            return sdf.format(netDate)
        }

        fun getDuration(duration: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
            val hours = TimeUnit.MILLISECONDS.toHours(duration)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(duration)

            val convertMinutes = String.format(
                "%02d", minutes -
                        TimeUnit.HOURS.toMinutes(hours)
            )
            val convertSeconds = String.format(
                "%02d", seconds -
                        TimeUnit.MINUTES.toSeconds(minutes)
            )

            return "${convertMinutes}:${convertSeconds}"
        }
    }
}





