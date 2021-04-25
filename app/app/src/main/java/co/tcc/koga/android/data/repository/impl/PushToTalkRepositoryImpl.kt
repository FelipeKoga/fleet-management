package co.tcc.koga.android.data.repository.impl

import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.network.payload.PushToTalkPayload
import co.tcc.koga.android.data.network.payload.PushToTalkResponse
import co.tcc.koga.android.data.network.payload.RecevingPTT
import co.tcc.koga.android.data.network.socket.*
import co.tcc.koga.android.data.repository.PushToTalkRepository
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class PushToTalkRepositoryImpl @Inject constructor(private val webSocketService: WebSocketService) :
    PushToTalkRepository {

    private val isReceiving = PublishSubject.create<RecevingPTT>()
    private val isRecording = PublishSubject.create<Boolean>()

    private var receivers: List<String>? = null
    private var chatId: String? = null
    private val currentUser = Client.getInstance().currentUser

    override fun start(chatId: String, receivers: List<String>?) {
        isRecording.onNext(true)
        this.receivers = receivers
        this.chatId = chatId
        val payload = WebSocketPayload(
            WebSocketActions.PUSH_TO_TALK,
            PushToTalkPayload(
                PushToTalkActions.START_PUSH_TO_TALK,
                chatId,
                currentUser,
                receivers,
                null
            )
        )
        webSocketService.send(payload)
    }

    override fun stop() {
        isRecording.onNext(false)
        val payload = WebSocketPayload(
            WebSocketActions.PUSH_TO_TALK,
            PushToTalkPayload(
                PushToTalkActions.STOP_PUSH_TO_TALK,
                chatId as String,
                currentUser,
                receivers, null
            )
        )
        webSocketService.send(payload)
    }

    override fun send(
        inputData: String,
    ) {
        val payload = WebSocketPayload(
            WebSocketActions.PUSH_TO_TALK,
            PushToTalkPayload(
                PushToTalkActions.SEND_PUSH_TO_TALK,
                chatId as String,
                currentUser,
                receivers, inputData

            )
        )
        webSocketService.send(payload)
    }

    override fun setReceivingPTT(value: RecevingPTT) {
        isReceiving.onNext(value)
    }

    override fun receivingPTT(): Observable<RecevingPTT> {
        return isReceiving.hide()
    }

    override fun receive(): Observable<WebSocketMessage<PushToTalkResponse, PushToTalkActions>> {
        return webSocketService.observePushToTalk()
    }

    override fun isRecording(): Observable<Boolean> {
        return isRecording.hide()
    }

}