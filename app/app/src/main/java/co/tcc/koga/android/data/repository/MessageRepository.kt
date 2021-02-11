package co.tcc.koga.android.data.repository

import androidx.lifecycle.LiveData
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.domain.MessagesResponse
import co.tcc.koga.android.data.network.socket.MessageActions
import co.tcc.koga.android.data.network.socket.WebSocketMessage
import io.reactivex.Observable

interface MessageRepository {

    fun getMessages(
        chatId: String
    ): Observable<MessagesResponse>

    suspend fun sendMessage(
        message: MessageEntity
    ): MessageEntity

    fun observeMessageUpdated(): Observable<WebSocketMessage<MessageEntity, MessageActions>>

    suspend fun insertMessage(message: MessageEntity)
}