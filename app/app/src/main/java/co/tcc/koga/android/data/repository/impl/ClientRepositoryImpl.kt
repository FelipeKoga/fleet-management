package co.tcc.koga.android.data.repository.impl

import android.content.Context
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.dao.UserDAO
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.data.network.Service
import co.tcc.koga.android.data.network.Socket
import co.tcc.koga.android.data.networkBoundResource
import co.tcc.koga.android.data.repository.ClientRepository
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    val context: Context,
    private val userDao: UserDAO,
    private val service: Service,
) :
    ClientRepository {

    override fun initApp(
        onSignInState: () -> Unit,
        onSignedOutState: () -> Unit,
        onError: () -> Unit
    ) {
        Client.getInstance().initAWSClient(context, fun(isSignIn) {
            ThreadUtils.runOnUiThread {
                if (isSignIn) {
                    onSignInState()
                } else onSignedOutState()
            }
        }, fun() {
            ThreadUtils.runOnUiThread {
                onError()
            }
        })
    }

    @ExperimentalCoroutinesApi
    override fun getCurrentUser(): Flow<Resource<UserEntity>> {

        return networkBoundResource(
            fetchFromLocal = { userDao.getCurrentUser(Client.getInstance().username()) },
            shouldFetchFromRemote = {
                if (it !== null) {
                    Client.getInstance().currentUser = it
                    false
                } else {
                    true
                }
            },
            fetchFromRemote = { service.getCurrentUser(Client.getInstance().username()) },
            processRemoteResponse = { },
            saveRemoteData = {
                Client.getInstance().currentUser = it
                userDao.setCurrentUser(it)
            },
            onFetchFailed = { _, _ -> println("Failed") }
        ).flowOn(Dispatchers.IO)
    }


    override suspend fun signOut() {
//        Thread { appDatabase.clearAllTables() }.start()
        Socket.closeConnection()
        Client.getInstance().signOut()
    }

    override fun initWebSocket() {
        Socket.initWebSocket()
    }

}