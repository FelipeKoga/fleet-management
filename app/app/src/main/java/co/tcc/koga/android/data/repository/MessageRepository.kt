package co.tcc.koga.android.data.repository

import android.os.Message
import androidx.lifecycle.LiveData
import co.tcc.koga.android.data.database.entity.MessageEntity

interface MessageRepository {

    suspend fun insert(
        id: String,
        message: String,
        senderId: String,
        recipientId: String,
        chatId: String
    ): MessageEntity

    suspend fun insert(message: MessageEntity)

    suspend fun replaceMessage(message: MessageEntity)

    suspend fun insertAll(messages: List<MessageEntity>, chatId: String)

    suspend fun getMessages(chatId: String): List<MessageEntity>

    suspend fun getMessagesFromNetwork(
        companyId: String,
        userId: String,
        chatId: String
    ): List<MessageEntity>

    suspend fun sendMessage(
        text: String,
        senderId: String,
        recipientId: String,
        chatId: String
    ): MessageEntity

    fun receiveMessage(): LiveData<MessageEntity>

    fun messageSent(): LiveData<MessageEntity>
}