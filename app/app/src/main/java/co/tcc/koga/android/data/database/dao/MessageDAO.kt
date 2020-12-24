package co.tcc.koga.android.data.database.dao

import androidx.room.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(messages: List<MessageEntity>)

    @Query("DELETE FROM message where chatId = :chatId")
    fun deleteAll(chatId: String)

    @Query("SELECT * FROM message WHERE chatId = :chatId")
    fun getAll(chatId: String): Observable<List<MessageEntity>>
}