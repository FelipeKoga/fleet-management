package co.tcc.koga.android.data.repository.impl

import co.tcc.koga.android.data.database.dao.ChatDAO
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.network.retrofit.Service
import co.tcc.koga.android.data.network.payload.NewChatPayload
import co.tcc.koga.android.data.network.payload.OpenChatPayload
import co.tcc.koga.android.data.network.socket.*
import co.tcc.koga.android.data.repository.ChatsRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class ChatsRepositoryImpl @Inject constructor(
    private val service: Service,
    private val chatDao: ChatDAO,
    private val webSocketService: WebSocketService
) : ChatsRepository {


    private fun getChatsDatabase(): Observable<List<ChatEntity>> {
        return chatDao.getAll().subscribeOn(Schedulers.computation())
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
        members: List<Any>,
        groupName: String,
        avatar: String,
    ): ChatEntity {
//        val currentUser = Client.getInstance().currentUser
//        val usersEntity = members.map {
//            UserEntity(it.username, it.email, it.fullName, it.phone, it.companyId, it.avatar)
//        }
//        val newChat = service.createChat(
//            currentUser.username,
//            currentUser.companyId,
//            NewChatPayload("", false, groupName, avatar, currentUser.username, usersEntity)
//        )
//        chatDao.insert(newChat)
        return ChatEntity("", 0)

    }

    override suspend fun openChat(chatId: String) {
        webSocketService.send(
            WebSocketPayload(
                WebSocketActions.OPEN_MESSAGES,
                OpenChatPayload(Client.getInstance().username(), chatId)
            )
        )
        chatDao.viewedMessages(chatId)
    }

    override fun updateChat(chat: ChatEntity) {
        chatDao.update(chat)
    }

    override fun observeChatUpdates(): Observable<WebSocketMessage<ChatEntity, ChatActions>> {
        return webSocketService.observeChat()
    }

    override fun observeUserUpdates(): Observable<WebSocketMessage<UserEntity, UserActions>> {
        return webSocketService.observeUser()
    }

}