package co.tcc.koga.android.data.repository

import androidx.lifecycle.LiveData
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.socket.ChatActions
import co.tcc.koga.android.data.network.socket.MessageActions
import co.tcc.koga.android.data.network.socket.UserActions
import co.tcc.koga.android.data.network.socket.WebSocketMessage
import io.reactivex.Observable

interface ChatsRepository {

    fun getChats(): Observable<List<ChatEntity>>

    suspend fun createChat(
        member_username: String,
    ): ChatEntity


    suspend fun createGroup(
        members: List<Any>,
        groupName: String,
        avatar: String,
    ): ChatEntity

    suspend fun openChat(chatId: String)

    fun updateChat(chat: ChatEntity)

    fun observeChatUpdates(): Observable<WebSocketMessage<ChatEntity, ChatActions>>

    fun observeUserUpdates(): Observable<WebSocketMessage<UserEntity, UserActions>>

}