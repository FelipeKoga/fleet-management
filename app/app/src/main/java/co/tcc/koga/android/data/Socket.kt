package co.tcc.koga.android.data

import co.tcc.koga.android.domain.ResquestSendMessage
import com.google.gson.Gson
import okhttp3.*

class Socket {

    fun sendAction(requestSendMessage: ResquestSendMessage) {
        getSocket()?.send(Gson().toJson(ResquestSendMessage("Ola 2", "sendMessage")))
    }

    companion object {
        private val request =
            Request.Builder().url(" wss://3x7sch9ll8.execute-api.us-east-1.amazonaws.com/dev")
                .build()
        private var webSocket: WebSocket? = null
        fun getSocket(): WebSocket? {
            if (webSocket === null) {
                webSocket = OkHttpClient().newWebSocket(request, object : WebSocketListener() {
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        super.onMessage(webSocket, text)
                        println("ON MESSAGE")
                        println(text)
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        super.onClosed(webSocket, code, reason)
                        println("ON CLOSED")
                    }
                })
            }
            return webSocket
        }
    }


}