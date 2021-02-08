package co.tcc.koga.android.data.network.socket


import java.io.Serializable


data class WebSocketPayload(
    var action: WebSocketActions,
    var body: Any
) : Serializable