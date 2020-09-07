package co.tcc.koga.android.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.tcc.koga.android.data.database.entity.ContactEntity

@Dao
interface ContactDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setCurrentUser(userEntity: ContactEntity)

    @Query("SELECT * FROM contact WHERE email = :email ")
    suspend fun getCurrentUser(email: String): ContactEntity?
}