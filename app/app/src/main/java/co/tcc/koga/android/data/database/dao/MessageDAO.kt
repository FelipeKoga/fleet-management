package co.tcc.koga.android.data.database.dao

import androidx.room.*
import co.tcc.koga.android.data.database.entity.MessageEntity

@Dao
interface MessageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Query("UPDATE message SET id = :id where chatId = :chatId AND createdAt = :createdAt")
    suspend fun replaceMessage(id: String, chatId: String, createdAt: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("DELETE FROM message where chatId = :chatId")
    suspend fun deleteAll(chatId: String)

    @Query("SELECT * FROM message WHERE chatId = :chatId")
    suspend fun getAll(chatId: String): List<MessageEntity>
}