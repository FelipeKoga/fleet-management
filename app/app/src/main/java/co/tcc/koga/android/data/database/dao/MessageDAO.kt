package co.tcc.koga.android.data.database.dao

import androidx.room.*
import co.tcc.koga.android.data.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Query("UPDATE message SET messageId = :messageId where chatId = :chatId AND createdAt = :createdAt")
    suspend fun replaceMessage(messageId: String, chatId: String, createdAt: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(messages: List<MessageEntity>)

    @Query("DELETE FROM message where chatId = :chatId")
    suspend fun deleteAll(chatId: String)

    @Query("SELECT * FROM message WHERE chatId = :chatId")
    fun getAll(chatId: String): Flow<List<MessageEntity>>
}