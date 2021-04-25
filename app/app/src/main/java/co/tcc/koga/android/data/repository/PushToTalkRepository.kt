package co.tcc.koga.android.data.repository

import co.tcc.koga.android.data.network.payload.PushToTalkResponse
import co.tcc.koga.android.data.network.payload.RecevingPTT
import co.tcc.koga.android.data.network.socket.PushToTalkActions
import co.tcc.koga.android.data.network.socket.WebSocketMessage
import io.reactivex.Observable

interface PushToTalkRepository {
    fun start(chatId: String, receivers: List<String>?)
    fun stop()
    fun send(inputData: String)
    fun receive(): Observable<WebSocketMessage<PushToTalkResponse, PushToTalkActions>>
    fun setReceivingPTT(value: RecevingPTT)
    fun receivingPTT(): Observable<RecevingPTT>
    fun isRecording(): Observable<Boolean>
}