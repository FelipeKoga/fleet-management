package co.tcc.koga.android.data.repository.impl

import android.content.Context
import co.tcc.koga.android.data.database.AppDatabase
import co.tcc.koga.android.data.database.dao.UserDAO
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.data.network.retrofit.Service
import co.tcc.koga.android.data.network.socket.WebSocketService
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.utils.Constants
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils
import com.amazonaws.services.cognitoidentityprovider.model.InvalidParameterException
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    val context: Context,
    private val userDao: UserDAO,
    private val service: Service,
    private val appDatabase: AppDatabase,
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

    private fun getUserDatabase(): Observable<UserEntity> {
        return userDao.getCurrentUser(Client.getInstance().username())
            .subscribeOn(Schedulers.computation()).doOnNext { user ->
                Client.getInstance().subCurrentUser.onNext(user)
                Client.getInstance().currentUser = user
            }
    }

    private fun getUserNetwork(): Observable<UserEntity> {
        return service.getCurrentUser(
            Client.getInstance().username(),
        )
            .subscribeOn(Schedulers.newThread())
            .doOnNext { user ->
                Client.getInstance().subCurrentUser.onNext(user)
                Client.getInstance().currentUser = user
                userDao.setCurrentUser(user)

            }.subscribeOn(Schedulers.computation())

    }

    override fun getCurrentUser(networkOnly: Boolean): Observable<UserEntity> {
        if (networkOnly) {
            return getUserNetwork()
        }
        return Observable.merge(getUserDatabase(), getUserNetwork())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun signIn(
        username: String,
        password: String,
        loggedIn: () -> Unit,
        unauthorized: () -> Unit,
        error: () -> Unit
    ) {

        Client.getInstance().signIn(username, password, loggedIn, fun(e) {
            if (e is NotAuthorizedException) {
                unauthorized()
            } else {
                error()
            }
        })
    }

    override fun sendCode(username: String, onSuccess: () -> Unit, onError: () -> Unit) {
        Client.getInstance().sendCode(username, onSuccess, fun(_) {
            onError()
        })

    }

    override fun confirmChangePassword(
        password: String,
        code: String,
        onSuccess: () -> Unit,
        notLongEnough: () -> Unit,
        onError: () -> Unit
    ) {
        Client.getInstance().confirmChangePassword(password, code, onSuccess, fun(e) {
            if (e is InvalidParameterException) {
                notLongEnough()
            } else {
                onError()
            }
        })
    }

    override fun observeAuthStatus(): Observable<Constants.AuthStatus> {
        return Client.getInstance().authStatus()
    }

    override fun observeCurrentUser(): Observable<UserEntity> {
        return Client.getInstance().observeCurrentUser()
    }


    override suspend fun signOut() {
        Thread { appDatabase.clearAllTables() }.start()
        Client.getInstance().signOut()
    }

    override fun user(): UserEntity {
        return Client.getInstance().currentUser
    }

}