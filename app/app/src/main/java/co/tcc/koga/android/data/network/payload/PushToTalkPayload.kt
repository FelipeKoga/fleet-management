package co.tcc.koga.android.data.network.payload

import co.tcc.koga.android.data.network.socket.PushToTalkActions
import co.tcc.koga.android.data.network.socket.WebSocketActions
import java.io.Serializable

data class PushToTalkPayload(
    val type: PushToTalkActions? = null,
    val chatId: String,
    val username: String,
    val receiver: String? = null,
    val receivers: List<String>? = null,
    val inputData: String? = null,
    val length: Int? = null
): Serializable