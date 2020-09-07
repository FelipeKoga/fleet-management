package co.tcc.koga.android.data.repository

import co.tcc.koga.android.data.database.entity.ChatEntity

interface ChatsRepository {

    suspend fun getAllChats(): List<ChatEntity>

    suspend fun getChatsFromNetwork(userId: String, companyId: String): List<ChatEntity>

}