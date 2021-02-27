package co.tcc.koga.android.data.network.payload

import java.io.Serializable

data class NewChatPayload(
    val withUsername: String
) : Serializable