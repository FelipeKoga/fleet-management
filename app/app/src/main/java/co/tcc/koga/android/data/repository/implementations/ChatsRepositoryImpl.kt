package co.tcc.koga.android.data.repository.implementations

import co.tcc.koga.android.data.database.dao.ChatDAO
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.network.api.NetworkUtils
import co.tcc.koga.android.data.repository.ChatsRepository
import javax.inject.Inject


class ChatsRepositoryImpl @Inject constructor(private val chatDAO: ChatDAO) : ChatsRepository {
    override suspend fun getAllChats(): List<ChatEntity> {
        return chatDAO.getAll()
    }

    override suspend fun getChatsFromNetwork(userId: String, companyId: String): List<ChatEntity> {
        val chats = NetworkUtils.api.getChats(
            companyId,
            userId
        )
        println(chats)
        chatDAO.insertAll(chats)
        return getAllChats()
    }

}