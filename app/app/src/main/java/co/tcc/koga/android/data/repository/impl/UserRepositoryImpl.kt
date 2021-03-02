package co.tcc.koga.android.data.repository.impl

import co.tcc.koga.android.data.database.dao.UserDAO
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.network.payload.LocationPayload
import co.tcc.koga.android.data.network.payload.UploadResponse
import co.tcc.koga.android.data.network.payload.UploadUrlPayload
import co.tcc.koga.android.data.network.retrofit.Service
import co.tcc.koga.android.data.network.socket.WebSocketActions
import co.tcc.koga.android.data.network.socket.WebSocketPayload
import co.tcc.koga.android.data.network.socket.WebSocketService
import co.tcc.koga.android.data.repository.UserRepository
import co.tcc.koga.android.utils.Constants
import io.reactivex.Observable
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDAO,
    private val service: Service,
    private val webSocketService: WebSocketService,
) : UserRepository {

    override suspend fun getLocalUsers(): List<UserEntity> {
        val user = Client.getInstance().currentUser
        return userDao.suspendGetAll(user.companyId, user.username)
    }

    override fun getUsers(): Observable<List<UserEntity>> {
        val user = Client.getInstance().currentUser
        return service.getUsers(user.companyId)
    }

    override fun sendLocation(latitude: Double, longitude: Double) {
        webSocketService.send(
            WebSocketPayload(
                WebSocketActions.SEND_LOCATION,
                LocationPayload(
                    latitude,
                    longitude,
                    Client.getInstance().currentUser.username,
                    Client.getInstance().currentUser.companyId
                )
            )
        )
    }

    override suspend fun updateUser(userEntity: UserEntity): Observable<UserEntity> {
        userDao.insert(userEntity)
        return service.updateUser(userEntity.companyId, userEntity.username, userEntity).doOnNext {
            Client.getInstance().currentUser = it
            Client.getInstance().subCurrentUser.onNext(it)
        }
    }

    override suspend fun updateLocalUser(userEntity: UserEntity) {
        userDao.insert(userEntity)
    }

    override fun getUploadPhotoUrl(key: String): Observable<UploadResponse> {
        val payload = UploadUrlPayload(key)
        return service.uploadUrl(payload)
    }

    override fun uploadPhoto(file: File, url: String): Observable<ResponseBody> {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val retrofit = Retrofit.Builder()
            .client(
                OkHttpClient
                    .Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .retryOnConnectionFailure(true).build()
            )
            .baseUrl(Constants.ApiURL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(Service::class.java)

        val avatarBody = RequestBody.create(MediaType.parse("image/jpeg"), file)
        return service.uploadFile(url, avatarBody)
    }

    override fun observeCurrentUser(): Observable<UserEntity> {
        return Client.getInstance().subCurrentUser
    }
}