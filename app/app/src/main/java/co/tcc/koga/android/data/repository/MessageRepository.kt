package co.tcc.koga.android.data.repository

import androidx.lifecycle.LiveData
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    fun getMessages(
        chatId: String
    ): Flow<Resource<List<MessageEntity>>>

    suspend fun sendMessage(
        message: MessageEntity
    ): MessageEntity

    fun messageReceived(): LiveData<MessageEntity>

    fun messageSent(): LiveData<MessageEntity>

    suspend fun insertMessage(message: MessageEntity)
}