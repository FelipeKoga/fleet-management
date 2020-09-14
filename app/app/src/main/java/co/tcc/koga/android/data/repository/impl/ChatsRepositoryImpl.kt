package co.tcc.koga.android.data.repository.impl

import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.dao.ChatDAO
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.data.network.Service
import co.tcc.koga.android.data.network.payload.NewChatPayload
import co.tcc.koga.android.data.networkBoundResource
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class ChatsRepositoryImpl @Inject constructor(
    private val apiService: Service,
    private val chatDAO: ChatDAO
) : ChatsRepository {
    @ExperimentalCoroutinesApi
    override fun getChats(): Flow<Resource<List<ChatEntity>>> {
        val currentUser = Client.getInstance().currentUser
        return networkBoundResource(
            fetchFromLocal = { chatDAO.getAll() },
            shouldFetchFromRemote = {
                true
            },
            fetchFromRemote = { apiService.getChats(currentUser.companyId, currentUser.username) },
            processRemoteResponse = { println(it) },
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

}