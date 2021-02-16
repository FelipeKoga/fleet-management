package co.tcc.koga.android.data.repository

import co.tcc.koga.android.data.database.entity.UserEntity
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    //    fun getUsers(): Observable<List<UserEntity>>
    suspend fun getLocalUsers(): List<UserEntity>

    fun sendLocation(
        latitude: Double, longitude: Double
    )
}