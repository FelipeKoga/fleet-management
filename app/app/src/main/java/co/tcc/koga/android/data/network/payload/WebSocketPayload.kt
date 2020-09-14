package co.tcc.koga.android.data.network.payload


import java.io.Serializable

data class WebSocketPayload(
    var action: String,
    var data: Any
    ): Serializable