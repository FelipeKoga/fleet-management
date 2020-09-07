package co.tcc.koga.android.utils

import java.text.SimpleDateFormat
import java.util.*

fun getHourByTimestamp(timestamp: String): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val netDate = Date(timestamp.toLong() * 1000)
    return sdf.format(netDate)
}

fun getDateByTimestamp(timestamp: String): String {
    val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
    val netDate = Date(timestamp.toLong() * 1000)
    return sdf.format(netDate)
}

