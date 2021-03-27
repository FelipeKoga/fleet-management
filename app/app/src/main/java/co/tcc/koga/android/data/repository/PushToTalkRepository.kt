package co.tcc.koga.android.data.repository

import co.tcc.koga.android.data.network.payload.PushToTalkResponse
import co.tcc.koga.android.data.network.socket.PushToTalkActions
import co.tcc.koga.android.data.network.socket.WebSocketMessage
import io.reactivex.Observable

interface PushToTalkRepository {
    fun start(chatId: String, receiver: String?, receivers: List<String>?)
    fun stop(chatId: String, receiver: String?, receivers: List<String>?)
    fun send(chatId: String, receiver: String?, receivers: List<String>?, inputData: String, length: Int)
    fun receive(): Observable<WebSocketMessage<PushToTalkResponse, PushToTalkActions>>
    fun setReceivingPTT(value: Boolean)
    fun receivingPTT(): Observable<Boolean>
}