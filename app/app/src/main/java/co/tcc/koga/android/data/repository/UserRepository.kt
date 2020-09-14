package co.tcc.koga.android.data.repository

import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(): Flow<Resource<List<UserEntity>>>
    suspend fun getLocalUsers(): List<UserEntity>
}