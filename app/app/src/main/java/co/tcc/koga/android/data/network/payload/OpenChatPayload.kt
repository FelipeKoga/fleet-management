package co.tcc.koga.android.data.network.payload

import java.io.Serializable

data class OpenChatPayload(
    val username: String,
    val chatId: String
): Serializable