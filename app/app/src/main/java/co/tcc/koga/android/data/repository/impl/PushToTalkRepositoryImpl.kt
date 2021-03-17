package co.tcc.koga.android.data.repository.impl

import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.network.payload.PushToTalkPayload
import co.tcc.koga.android.data.network.payload.PushToTalkResponse
import co.tcc.koga.android.data.network.socket.*
import co.tcc.koga.android.data.repository.PushToTalkRepository
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import io.reactivex.Observable
import javax.inject.Inject

class PushToTalkRepositoryImpl @Inject constructor(private val webSocketService: WebSocketService) :
    PushToTalkRepository {


    override fun start(chatId: String, receiver: String?, receivers: List<String>?) {
        val username = Client.getInstance().username()
        val payload = WebSocketPayload(
            WebSocketActions.PUSH_TO_TALK,
            PushToTalkPayload(
                PushToTalkActions.START_PUSH_TO_TALK,
                chatId,
                username,
                receiver,
                receivers, null, null
            )
        )

        println("START PUSH TO TALK REPOSITORY: $payload")
        webSocketService.send(payload)
    }

    override fun stop(chatId: String, receiver: String?, receivers: List<String>?) {
        val username = Client.getInstance().username()
        val payload = WebSocketPayload(
            WebSocketActions.PUSH_TO_TALK,
            PushToTalkPayload(
                PushToTalkActions.STOP_PUSH_TO_TALK,
                chatId,
                username,
                receiver,
                receivers, null, null
            )
        )
        println("STOP PUSH TO TALK REPOSITORY: $payload")

            webSocketService.send(payload)
    }

    override fun send(
        chatId: String,
        receiver: String?,
        receivers: List<String>?,
        inputData: String,
        length: Int
    ) {
        val username = Client.getInstance().username()
        val payload = WebSocketPayload(
            WebSocketActions.PUSH_TO_TALK,
            PushToTalkPayload(
                PushToTalkActions.SEND_PUSH_TO_TALK,
                chatId,
                username,
                receiver,
                receivers, inputData, length

            )
        )

        println("SEND PUSH TO TALK REPOSITORY: $payload")
        webSocketService.send(payload)
    }

    override fun receive(): Observable<WebSocketMessage<PushToTalkResponse, PushToTalkActions>> {
        return webSocketService.observePushToTalk()
    }

}