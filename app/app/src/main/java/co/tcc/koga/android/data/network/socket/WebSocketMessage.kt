package co.tcc.koga.android.data.network.socket

import java.io.Serializable

data class WebSocketMessage<T, K>(
    val action: K,
    val body: T
) : Serializable
