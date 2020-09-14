package co.tcc.koga.android.data.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.payload.WebSocketPayload
import co.tcc.koga.android.utils.CONSTANTS
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.google.gson.Gson
import okhttp3.*

class Socket {
    companion object {
        private lateinit var webSocket: WebSocket
        private var isConnected: Boolean = false
        private val MESSAGE_RECEIVED_EVENT = MutableLiveData<MessageEntity>()
        private val MESSAGE_SENT_EVENT = MutableLiveData<MessageEntity>()

        val messageReceived: LiveData<MessageEntity> get() = MESSAGE_RECEIVED_EVENT
        val messageSentEvent: LiveData<MessageEntity> get() = MESSAGE_SENT_EVENT


        fun initWebSocket() {
            if (!isConnected) {

                val request =
                    Request.Builder()
                        .url(
                            "${CONSTANTS.WEBSOCKET_URL}?username=${
                                Client.getInstance().username()
                            }"
                        )
                        .build()

                webSocket = OkHttpClient().newWebSocket(request, object : WebSocketListener() {
                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        super.onFailure(webSocket, t, response)
                        println(t.message)
                        isConnected = false
                    }

                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)
                        println("on open")
                        isConnected = true
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        super.onMessage(webSocket, text)
                        println(text)
                        if (text.isNotEmpty()) {
                            val webSocketPayload =
                                Gson().fromJson(text, WebSocketPayload::class.java)
                            println(webSocketPayload)
                            handleWebSocketMessage(webSocketPayload)
                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        super.onClosed(webSocket, code, reason)
                        isConnected = false
                        println("ON CLOSED")
                    }
                })
            }

        }

        fun getConnection(): WebSocket {
            if (!Companion::webSocket.isInitialized) {
                initWebSocket()
            }
            return webSocket
        }

        fun closeConnection() {
            webSocket.close(1000, "CLOSED")
        }

        private fun handleWebSocketMessage(response: WebSocketPayload) {
            ThreadUtils.runOnUiThread {
                val gson = Gson()
                val data = gson.toJson(response.data)
                when (response.action) {
//                    "connected", "disconnect", "post-user", "delete-user" -> socketResponse.value =
//                        gson.fromJson(data, User::class.java)
                    "message_receive" -> {
                        MESSAGE_RECEIVED_EVENT.value = gson.fromJson(data, MessageEntity::class.java)
                    }
                    "message_sent" -> {
                        MESSAGE_SENT_EVENT.value = gson.fromJson(data, MessageEntity::class.java)
                    }
                }
            }

        }
    }


}