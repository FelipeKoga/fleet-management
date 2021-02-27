package co.tcc.koga.android.data.repository

import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.payload.UploadResponse
import io.reactivex.Observable
import okhttp3.ResponseBody
import java.io.File

interface UserRepository {
    fun getUsers(): Observable<List<UserEntity>>
    suspend fun getLocalUsers(): List<UserEntity>

    fun sendLocation(
        latitude: Double, longitude: Double
    )

    fun uploadPhoto(file: File, url: String): Observable<ResponseBody>

    fun getUploadPhotoUrl(key: String):
            Observable<UploadResponse>

    suspend fun updateUser(userEntity: UserEntity): Observable<UserEntity>

    suspend fun updateLocalUser(userEntity: UserEntity)

    fun observeCurrentUser(): Observable<UserEntity>
}