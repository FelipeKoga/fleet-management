package co.tcc.koga.android.data.repository

import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.socket.ChatActions
import co.tcc.koga.android.data.network.socket.UserActions
import co.tcc.koga.android.data.network.socket.WebSocketMessage
import co.tcc.koga.android.data.domain.ChatsResponse
import io.reactivex.Observable

interface ChatsRepository {

    fun getChats(): Observable<ChatsResponse>

    suspend fun createChat(
        member_username: String,
    ): ChatEntity


    suspend fun createGroup(
        members: List<UserEntity>,
        groupName: String,
        avatar: String,
    ): ChatEntity

    suspend fun openChat(chatId: String)

    suspend fun insertChat(chat: ChatEntity)

    suspend fun updateChat(chat: ChatEntity)

    fun observeChatUpdates(): Observable<WebSocketMessage<ChatEntity, ChatActions>>

    fun observeUserUpdates(): Observable<WebSocketMessage<UserEntity, UserActions>>

}