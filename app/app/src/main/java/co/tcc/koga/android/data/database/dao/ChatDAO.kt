package co.tcc.koga.android.data.database.dao

import androidx.room.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity)

    @Update
    fun update(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(chats: List<ChatEntity>)

    @Query("SELECT * FROM chat")
    fun getAll(): Flow<List<ChatEntity>>

    @Query("DELETE FROM chat")
    fun deleteAll()
}