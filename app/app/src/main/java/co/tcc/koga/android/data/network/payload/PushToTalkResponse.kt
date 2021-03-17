package co.tcc.koga.android.data.network.payload

import java.io.Serializable

data class PushToTalkResponse(
    val chatId: String,
    val username: String,
    val inputData: String?,
    val length: Int?
): Serializable