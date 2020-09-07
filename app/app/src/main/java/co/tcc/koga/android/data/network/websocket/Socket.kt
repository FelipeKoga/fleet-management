package co.tcc.koga.android.data.network.websocket

import androidx.lifecycle.MutableLiveData
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.domain.Contact
import co.tcc.koga.android.domain.WSResponse
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.google.gson.Gson
import okhttp3.*

class Socket {
    companion object {
        private var isConnected: Boolean = false
        private lateinit var webSocket: WebSocket
        val newMessage = MutableLiveData<MessageEntity>()
        val messageSent = MutableLiveData<MessageEntity>()
        val newConnected = MutableLiveData<Contact>()
        val newDisconnected = MutableLiveData<Contact>()
        val newUser = MutableLiveData<Contact>()
        val deletedUser = MutableLiveData<Contact>()

        fun initWebSocket(userId: String) {
            if (!isConnected) {
                val request =
                    Request.Builder()
                        .url(
                            "https://ljdjapabh7.execute-api.us-east-1.amazonaws.com/dev?userId=$userId"
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
                        if (text.isNotEmpty()) {
                            val obj = Gson().fromJson(text, WSResponse::class.java)
                            if (obj.action.isNotEmpty()) {
                                handleWebSocketMessage(obj)
                            }

                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        super.onClosed(webSocket, code, reason)
                        isConnected
                        println("ON CLOSED")
                    }
                })
            }

        }

        fun getConnection(userId: String): WebSocket {
            if (!::webSocket.isInitialized) {
                initWebSocket(userId)
            }
            return webSocket
        }

        fun closeConnection() {
            webSocket.close(1000, "CLOSED")
        }

        private fun handleWebSocketMessage(response: WSResponse) {
            ThreadUtils.runOnUiThread {
                val gson = Gson()
                val data = gson.toJson(response.data)
                when (response.action) {
                    "connected" -> newConnected.value = gson.fromJson(data, Contact::class.java)
                    "disconnect" -> newDisconnected.value =
                        gson.fromJson(data, Contact::class.java)
                    "post-user" -> newUser.value = gson.fromJson(data, Contact::class.java)
                    "delete-user" -> deletedUser.value = gson.fromJson(data, Contact::class.java)
                    "receive_message" -> {
                        newMessage.value = gson.fromJson(data, MessageEntity::class.java)
                    }
                    "message_sent" -> {
                        messageSent.value = gson.fromJson(data, MessageEntity::class.java)
                    }
                }
            }

        }
    }


}