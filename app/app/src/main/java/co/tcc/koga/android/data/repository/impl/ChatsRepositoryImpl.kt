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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType


class ChatsRepositoryImpl @Inject constructor(
    private val apiService: Service,
    private val chatDAO: ChatDAO,
    private val messageDAO: MessageDAO
) : ChatsRepository {
    @ExperimentalCoroutinesApi
    override fun getChats(): Flow<Resource<List<ChatEntity>>> {
        val currentUser = Client.getInstance().currentUser
        return networkBoundResource(
            fetchFromLocal = { chatDAO.getAll() },
            shouldFetchFromRemote = {
                println(it)
                true
            },
            fetchFromRemote = { apiService.getChats(currentUser.companyId, currentUser.username) },
            processRemoteResponse = {
                println("From remote")
                println(it) },
            saveRemoteData = {
                chatDAO.deleteAll()
                chatDAO.insertAll(it)
            },
            onFetchFailed = { _, _ -> }
        ).flowOn(Dispatchers.IO)
    }


    override suspend fun createChat(
        member_username: String,
    ): ChatEntity {
        val currentUser = Client.getInstance().currentUser
        val newChat = apiService.createChat(
            currentUser.username,
            currentUser.companyId,
            NewChatPayload(member_username, true, "", "")
        )
        chatDAO.insert(newChat)
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
        val newChat = apiService.createChat(
            currentUser.username,
            currentUser.companyId,
            NewChatPayload("", false, groupName, avatar, currentUser.username, usersEntity)
        )
        chatDAO.insert(newChat)
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
        chatDAO.viewedMessages(chatId)
    }

    override suspend fun updateChat(messageEntity: MessageEntity, received: Boolean): ChatEntity {
        if (received) chatDAO.receivedNewMessage(messageEntity.chatId)
        chatDAO.updateLastMessage(messageEntity.chatId, messageEntity)
        messageDAO.insert(messageEntity)
        return chatDAO.getChat(messageEntity.chatId)
    }

    override fun messageSent(): LiveData<MessageEntity> {
        return Socket.messageSentEvent
    }

    override fun messageReceived(): LiveData<MessageEntity> {
        return Socket.messageReceived
    }


}