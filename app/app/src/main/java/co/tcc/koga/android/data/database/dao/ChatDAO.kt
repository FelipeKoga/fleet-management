package co.tcc.koga.android.data.database.dao

import androidx.room.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity)

    @Query("UPDATE chat SET newMessages = 0 WHERE id = :chatId ")
    suspend fun viewedMessages(chatId: String)

    @Query("UPDATE chat SET newMessages = newMessages + 1 WHERE id = :chatId ")
    suspend fun receivedNewMessage(chatId: String)

    @Query("SELECT * FROM chat WHERE id = :chatId")
    suspend fun getChat(chatId: String): ChatEntity

    @Update
    suspend fun update(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(chats: List<ChatEntity>)

    @Query("SELECT * FROM chat")
    fun getAll(): Observable<List<ChatEntity>>

    @Query("DELETE FROM chat")
    fun deleteAll()
}