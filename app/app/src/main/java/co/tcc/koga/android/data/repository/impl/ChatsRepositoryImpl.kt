package co.tcc.koga.android.data.repository.impl

import co.tcc.koga.android.data.database.dao.ChatDAO
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.network.retrofit.Service
import co.tcc.koga.android.data.network.payload.NewChatPayload
import co.tcc.koga.android.data.network.payload.OpenChatPayload
import co.tcc.koga.android.data.network.socket.*
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.domain.ChatsResponse
import co.tcc.koga.android.data.network.payload.NewGroupPayload
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class ChatsRepositoryImpl @Inject constructor(
    private val service: Service,
    private val chatDao: ChatDAO,
    private val webSocketService: WebSocketService
) : ChatsRepository {


    private fun getChatsDatabase(): Observable<ChatsResponse> {
        val comparator = Comparator { chat1: ChatEntity, chat2: ChatEntity ->
            if (chat1.messages.isEmpty()) return@Comparator 1

            if (chat1.messages.isNotEmpty() && chat2.messages.isEmpty()) return@Comparator -1;

            return@Comparator (chat2.messages.last()?.createdAt?.minus(chat1.messages.last()?.createdAt!!))!!.toInt();
        }
        return chatDao.getAll().subscribeOn(Schedulers.computation()).map { chats ->
            ChatsResponse(chats.filter { chat -> chat.messages.isNotEmpty() }
                .sortedWith(comparator), true)
        }
    }

    private fun getChatsNetwork(): Observable<ChatsResponse> {
        val user = Client.getInstance().currentUser
        return service.getChats(user.companyId, user.username).subscribeOn(Schedulers.newThread())
            .doOnNext { chats ->
                println(chats)
                chatDao.insertAll(chats)
            }.subscribeOn(Schedulers.newThread()).map { chats ->
                println(chats)
                ChatsResponse(chats, false)
            }
    }

    override fun getChats(): Observable<ChatsResponse> {
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
            NewChatPayload(member_username)
        )
        chatDao.insert(newChat)
        return newChat

    }

    override suspend fun createGroup(
        members: List<UserEntity>,
        groupName: String,
        avatar: String,
    ): ChatEntity {
        val currentUser = Client.getInstance().currentUser
        val newChat = service.createGroup(
            currentUser.username,
            currentUser.companyId,
            NewGroupPayload(groupName, members.map { user -> user.username })
        )

        chatDao.insert(newChat)
        return newChat
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

    override suspend fun updateChat(chat: ChatEntity) {
        chatDao.update(chat)
    }

    override fun observeChatUpdates(): Observable<WebSocketMessage<ChatEntity, ChatActions>> {
        return webSocketService.observeChat()
    }

    override suspend fun insertChat(chat: ChatEntity) {
        chatDao.insert(chat)
    }

    override fun observeUserUpdates(): Observable<WebSocketMessage<UserEntity, UserActions>> {
        return webSocketService.observeUser()
    }

}