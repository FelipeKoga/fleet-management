package co.tcc.koga.android.data.repository.implementations

import android.content.Context
import co.tcc.koga.android.data.database.AppDatabase
import co.tcc.koga.android.data.database.dao.ContactDAO
import co.tcc.koga.android.data.database.entity.ContactEntity
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.data.network.api.NetworkUtils
import co.tcc.koga.android.data.network.websocket.Socket
import co.tcc.koga.android.data.repository.ClientRepository
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    val context: Context,
    private val appDatabase: AppDatabase,
    private val contactDAO: ContactDAO
) :
    ClientRepository {

    override fun initApp(
        onSignInState: () -> Unit,
        onSignedOutState: () -> Unit,
        onError: () -> Unit
    ) {
        Client.getInstance().initAWSClient(context, fun(isSignIn) {
            ThreadUtils.runOnUiThread {
                if (isSignIn)  {
                    onSignInState()
                }
                else onSignedOutState()
            }
        }, fun() {
            ThreadUtils.runOnUiThread {
                onError()
            }
        })
    }



    override suspend fun getCurrentUserFromLocal(): ContactEntity? {
        return contactDAO.getCurrentUser(Client.getInstance().username())
    }

    override suspend fun getCurrentUserFromRemote(): ContactEntity {
        contactDAO.setCurrentUser(NetworkUtils.api.getCurrentUser(Client.getInstance().username()))
        return getCurrentUserFromLocal() as ContactEntity
    }

    override suspend fun signOut() {
        Thread { appDatabase.clearAllTables() }.start()
        Socket.closeConnection()
        Client.getInstance().signOut()
    }

    override fun initWebSocket(userId: String) {
        Socket.initWebSocket(userId)
    }


}