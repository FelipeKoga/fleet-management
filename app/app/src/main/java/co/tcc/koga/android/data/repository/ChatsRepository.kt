package co.tcc.koga.android.data.repository

import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.domain.User
import kotlinx.coroutines.flow.Flow

interface ChatsRepository {

    fun getChats(): Flow<Resource<List<ChatEntity>>>

    suspend fun createChat(
        member_username: String,
    ): ChatEntity


    suspend fun createGroup(
        members: List<User>,
        groupName: String,
        avatar: String,
    ): ChatEntity
}