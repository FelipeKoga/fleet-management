package co.tcc.koga.android.data.repository.impl

import androidx.lifecycle.LiveData
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.dao.ChatDAO
import co.tcc.koga.android.data.database.dao.MessageDAO
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.data.network.Service
import co.tcc.koga.android.data.network.Socket
import co.tcc.koga.android.data.network.payload.NewChatPayload
import co.tcc.koga.android.data.network.payload.OpenChatPayload
import co.tcc.koga.android.data.network.payload.WebSocketPayload
import co.tcc.koga.android.data.networkBoundResource
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.domain.User
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.subscribeOn
import javax.inject.Inject
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType


class ChatsRepositoryImpl @Inject constructor(
    private val service: Service,
    private val chatDao: ChatDAO,
    private val messageDAO: MessageDAO
) : ChatsRepository {


    private fun getChatsDatabase(): Observable<List<ChatEntity>> {
        return chatDao.getAll()
            .subscribeOn(Schedulers.computation())
    }

    private fun getChatsNetwork(): Observable<List<ChatEntity>> {
        val user = Client.getInstance().currentUser
        return service.getChats(user.companyId, user.username).subscribeOn(Schedulers.newThread())
            .doOnNext { chats ->
                chatDao.insertAll(chats)
            }.subscribeOn(Schedulers.newThread())
    }

    override fun getChats(): Observable<List<ChatEntity>> {
        return Observable.merge(getChatsDatabase(), getChatsNetwork())
            .observeOn(AndroidSchedulers.mainThread())

    }


    override suspend fun createChat(
        member_username: String,
    ): ChatEntity {
        val currentUser = Client.getInstance().currentUser
        val newChat = service.createChat(
            currentUser.username,
            currentUser.companyId,
            NewChatPayload(member_username, true, "", "")
        )
        chatDao.insert(newChat)
        return newChat

    }

    override suspend fun createGroup(
        members: List<User>,
        groupName: String,
        avatar: String,
    ): ChatEntity {
        val currentUser = Client.getInstance().currentUser
        val usersEntity = members.map {
            UserEntity(it.username, it.email, it.fullName, it.phone, it.companyId, it.avatar)
        }
        val newChat = service.createChat(
            currentUser.username,
            currentUser.companyId,
            NewChatPayload("", false, groupName, avatar, currentUser.username, usersEntity)
        )
        chatDao.insert(newChat)
        return newChat

    }

    override suspend fun openChat(chatId: String) {
        val gson = Gson()
        val obj = OpenChatPayload(
            Client.getInstance().username(), chatId
        )
        println("OPEN CHAT")
        val payload = WebSocketPayload("open-messages", gson.toJson(obj))
        Socket.getConnection()
            .send(gson.toJson(payload))
        println("VIEWED MESSAGES")
        chatDao.viewedMessages(chatId)
    }

    override suspend fun updateChat(messageEntity: MessageEntity, received: Boolean): ChatEntity {
        if (received) chatDao.receivedNewMessage(messageEntity.chatId)
        chatDao.updateLastMessage(messageEntity.chatId, messageEntity)
        messageDAO.insert(messageEntity)
        return chatDao.getChat(messageEntity.chatId)
    }

    override fun messageSent(): LiveData<MessageEntity> {
        return Socket.messageSentEvent
    }

    override fun messageReceived(): LiveData<MessageEntity> {
        return Socket.messageReceived
    }


}