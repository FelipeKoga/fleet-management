package co.tcc.koga.android.data.database.dao

import androidx.room.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity)

    @Query("UPDATE chat SET lastMessage = :message  WHERE chatId = :chatId ")
    suspend fun updateLastMessage(chatId: String, message: MessageEntity)

    @Query("UPDATE chat SET newMessages = 0 WHERE chatId = :chatId ")
    suspend fun viewedMessages(chatId: String)

    @Query("UPDATE chat SET newMessages = newMessages + 1 WHERE chatId = :chatId ")
    suspend fun receivedNewMessage(chatId: String)

    @Query("SELECT * FROM chat WHERE chatId = :chatId")
    suspend fun getChat(chatId: String): ChatEntity

    @Update
    fun update(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(chats: List<ChatEntity>)

    @Query("SELECT * FROM chat WHERE lastMessage IS NOT NULL AND isPrivate ORDER BY lastMessage DESC")
    fun getAll(): Flow<List<ChatEntity>>

    @Query("DELETE FROM chat")
    fun deleteAll()
}