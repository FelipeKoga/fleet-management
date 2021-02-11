package co.tcc.koga.android.data.repository.impl

import co.tcc.koga.android.data.database.dao.MessageDAO
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.domain.MessagesResponse
import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.network.retrofit.Service
import co.tcc.koga.android.data.network.socket.*
import co.tcc.koga.android.data.repository.MessageRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val service: Service,
    private val messageDAO: MessageDAO,
    private val webSocketService: WebSocketService
) :
    MessageRepository {

    private fun getMessagesNetwork(chatId: String): Observable<MessagesResponse> {
        val user = Client.getInstance().currentUser
        return service.getMessages(user.companyId, user.username, chatId)
            .subscribeOn(Schedulers.newThread())
            .doOnNext { messages ->
                if (messages.isEmpty()) {
                    messageDAO.deleteAll(chatId)
                } else {
                    messageDAO.insertAll(messages)
                }
            }.subscribeOn(Schedulers.newThread()).map { messages ->
                MessagesResponse(messages, true)
            }
    }

    private fun getMessagesDatabase(chatId: String): Observable<MessagesResponse> {
        return messageDAO.getAll(chatId)
            .map { messages ->
                MessagesResponse(messages, true)
            }
            .subscribeOn(Schedulers.computation())
    }

    override fun getMessages(chatId: String): Observable<MessagesResponse> {
        return Observable.merge(getMessagesDatabase(chatId), getMessagesNetwork(chatId))
            .observeOn(AndroidSchedulers.mainThread())
    }

    override suspend fun sendMessage(
        message: MessageEntity
    ): MessageEntity {
        insertMessage(message)
        val payload = WebSocketPayload(WebSocketActions.SEND_MESSAGE, message)
        webSocketService.send(payload)
        return message
    }

    override suspend fun insertMessage(message: MessageEntity) {
        messageDAO.insert(message)
    }

    override fun observeMessageUpdated(): Observable<WebSocketMessage<MessageEntity, MessageActions>> {
        return webSocketService.observeMessage().subscribeOn(Schedulers.newThread())
    }
}