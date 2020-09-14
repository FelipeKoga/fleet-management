package co.tcc.koga.android.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.tcc.koga.android.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setCurrentUser(userEntity: UserEntity)

    @Query("SELECT * FROM user WHERE username = :username ")
    fun getCurrentUser(username: String): Flow<UserEntity>

    @Query("SELECT * FROM user WHERE companyId = :companyId AND username != :username ")
    fun getAll(companyId: String, username: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM user WHERE companyId = :companyId AND username != :username ")
    suspend fun suspendGetAll(companyId: String, username: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<UserEntity>)
}