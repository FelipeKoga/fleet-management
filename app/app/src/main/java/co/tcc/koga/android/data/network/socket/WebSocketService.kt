package co.tcc.koga.android.data.network.socket

import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Observable

interface WebSocketService {
    @Send
    fun send(message: WebSocketPayload)

    @Receive
    fun observeMessage(): Observable<WebSocketMessage<MessageEntity, MessageActions>>

    @Receive
    fun observeChat(): Observable<WebSocketMessage<ChatEntity, ChatActions>>

    @Receive
    fun observeUser(): Observable<WebSocketMessage<UserEntity, UserActions>>

}