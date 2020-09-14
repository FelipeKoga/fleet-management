package co.tcc.koga.android.data.repository.impl

import androidx.lifecycle.LiveData
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.dao.MessageDAO
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.*
import co.tcc.koga.android.data.network.payload.WebSocketPayload
import co.tcc.koga.android.data.networkBoundResource
import co.tcc.koga.android.data.repository.MessageRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val apiService: Service,
    private val messageDAO: MessageDAO
) :
    MessageRepository {

    @ExperimentalCoroutinesApi
    override fun getMessages(
        chatId: String
    ): Flow<Resource<List<MessageEntity>>> {
        val user = Client.getInstance().currentUser
        return networkBoundResource(
            fetchFromLocal = { messageDAO.getAll(chatId) },
            shouldFetchFromRemote = {
                true
            },
            fetchFromRemote = {
                apiService.getMessages(user.companyId, user.username, chatId)
            },
            processRemoteResponse = { },
            saveRemoteData = {
                messageDAO.insertAll(it)
            },
            onFetchFailed = { _, _ -> println("Failed") }
        ).flowOn(Dispatchers.IO)
    }

    override suspend fun sendMessage(
        message: MessageEntity
    ): MessageEntity {
        val gson = Gson()
        val payload = WebSocketPayload("send-message", gson.toJson(message))
        insertMessage(message)
        Socket.getConnection()
            .send(gson.toJson(payload))
        return message
    }

    override suspend fun insertMessage(message: MessageEntity) {
        messageDAO.insert(message)
    }

    override fun messageSent(): LiveData<MessageEntity> {
        return Socket.messageSentEvent
    }

    override fun messageReceived(): LiveData<MessageEntity> {
        return Socket.messageReceived
    }
}