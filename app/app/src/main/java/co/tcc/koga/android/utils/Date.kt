package co.tcc.koga.android.utils

import java.text.SimpleDateFormat
import java.util.*

fun getHour(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
    val netDate = Date(timestamp.toLong())
    return sdf.format(netDate)
}

fun getFullDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    val netDate = Date(timestamp.toLong())
    println(sdf.format(netDate))
    return sdf.format(netDate)
}

fun getDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM", Locale("pt", "BR"))
    val netDate = Date(timestamp.toLong())
    return sdf.format(netDate)
}




