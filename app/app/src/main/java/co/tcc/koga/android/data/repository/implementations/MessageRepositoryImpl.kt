package co.tcc.koga.android.data.repository.implementations

import androidx.lifecycle.LiveData
import co.tcc.koga.android.data.database.dao.MessageDAO
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.api.NetworkUtils
import co.tcc.koga.android.data.network.websocket.Socket
import co.tcc.koga.android.data.repository.MessageRepository
import co.tcc.koga.android.domain.WSRequest
import com.google.gson.Gson
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(private val messageDAO: MessageDAO) :
    MessageRepository {

    override suspend fun insert(
        id: String,
        message: String,
        senderId: String,
        recipientId: String,
        chatId: String
    ): MessageEntity {
        val entity = MessageEntity(
            id,
            message,
            senderId,
            recipientId,
            chatId
        )
        messageDAO.insert(entity)
        return entity
    }

    override suspend fun insert(message: MessageEntity) {
        messageDAO.insert(message)
    }

    override suspend fun replaceMessage(message: MessageEntity) {
        messageDAO.replaceMessage(message.id, message.chatId, message.createdAt)
    }

    override suspend fun insertAll(messages: List<MessageEntity>, chatId: String) {
        messageDAO.deleteAll(chatId)
        messageDAO.insertAll(messages)
    }

    override suspend fun getMessages(chatId: String): List<MessageEntity> {
        return messageDAO.getAll(chatId)
    }

    override suspend fun getMessagesFromNetwork(
        companyId: String,
        userId: String,
        chatId: String
    ): List<MessageEntity> {
        val messages = NetworkUtils.api.getMessages(companyId, userId, chatId)
        insertAll(messages, chatId)
        return getMessages(chatId)
    }

    override suspend fun sendMessage(
        text: String,
        senderId: String,
        recipientId: String,
        chatId: String
    ): MessageEntity {
        val message = MessageEntity("", text, senderId, recipientId, chatId)
        insert(message)
        val gson = Gson()
        Socket.getConnection(senderId)
            .send(gson.toJson(WSRequest("send-message", gson.toJson(message))))
        return message
    }

    override fun receiveMessage(): LiveData<MessageEntity> {
        return Socket.newMessage
    }

    override fun messageSent(): LiveData<MessageEntity> {
        return Socket.messageSent
    }
}