package co.tcc.koga.android.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.tcc.koga.android.data.database.entity.ChatEntity

@Dao
interface ChatDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<ChatEntity>)

    @Query("SELECT * FROM chat")
    suspend fun getAll(): List<ChatEntity>
}