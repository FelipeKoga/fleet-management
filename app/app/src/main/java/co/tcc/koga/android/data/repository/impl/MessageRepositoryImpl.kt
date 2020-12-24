package co.tcc.koga.android.data.repository.impl

import androidx.lifecycle.LiveData
import co.tcc.koga.android.data.database.dao.ChatDAO
import co.tcc.koga.android.data.database.dao.MessageDAO
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.*
import co.tcc.koga.android.data.network.payload.WebSocketPayload
import co.tcc.koga.android.data.repository.MessageRepository
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val service: Service,
    private val messageDAO: MessageDAO,
    private val chatDAO: ChatDAO
) :
    MessageRepository {


    private fun getMessagesNetwork(chatId: String): Observable<List<MessageEntity>> {
        val user = Client.getInstance().currentUser
        return service.getMessages(user.companyId, user.username, chatId)
            .subscribeOn(Schedulers.newThread())
            .doOnNext { messages ->
                if (messages.isEmpty()) {
                    messageDAO.deleteAll(chatId)
                } else {
                    messageDAO.insertAll(messages)
                }
            }.subscribeOn(Schedulers.newThread())
    }

    private fun getMessagesDatabase(chatId: String): Observable<List<MessageEntity>> {
        return messageDAO.getAll(chatId)
            .subscribeOn(Schedulers.computation())
    }

    override fun getMessages(chatId: String): Observable<List<MessageEntity>> {
        return Observable.merge(getMessagesDatabase(chatId), getMessagesNetwork(chatId))
            .observeOn(AndroidSchedulers.mainThread())
    }

    override suspend fun sendMessage(
        message: MessageEntity
    ): MessageEntity {
        val gson = Gson()
        val payload = WebSocketPayload("send-message", gson.toJson(message))
        insertMessage(message)
        println("SEND MESSAGE")
        println(message)
        Socket.getConnection()
            .send(gson.toJson(payload))
        return message
    }

    override suspend fun insertMessage(message: MessageEntity) {
        messageDAO.insert(message)
        chatDAO.updateLastMessage(message.chatId, message)
    }

    override fun messageSent(): LiveData<MessageEntity> {
        return Socket.messageSentEvent
    }

    override fun messageReceived(): LiveData<MessageEntity> {
        return Socket.messageReceived
    }
}