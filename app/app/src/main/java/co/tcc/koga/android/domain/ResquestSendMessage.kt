package co.tcc.koga.android.domain


import java.io.Serializable

data class ResquestSendMessage(
    var data: String,
    var action: String
): Serializable