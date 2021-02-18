package co.tcc.koga.android.data.repository.impl

import co.tcc.koga.android.data.database.dao.UserDAO
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.network.payload.LocationPayload
import co.tcc.koga.android.data.network.retrofit.Service
import co.tcc.koga.android.data.network.socket.WebSocketActions
import co.tcc.koga.android.data.network.socket.WebSocketPayload
import co.tcc.koga.android.data.network.socket.WebSocketService
import co.tcc.koga.android.data.repository.UserRepository
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDAO,
    private val service: Service,
    private val webSocketService: WebSocketService
) : UserRepository {

    override suspend fun getLocalUsers(): List<UserEntity> {
        val user = Client.getInstance().currentUser
        return userDao.suspendGetAll(user.companyId, user.username)
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
//        println("INSERT IN DAO")
        userDao.insert(userEntity)
//        println("passei")

        println(userEntity)
        return service.updateUser(userEntity.companyId, userEntity.username, userEntity).doOnNext {
                Client.getInstance().currentUser = it
            }

    }
}