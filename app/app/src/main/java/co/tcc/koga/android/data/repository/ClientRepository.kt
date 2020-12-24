package co.tcc.koga.android.data.repository


import co.tcc.koga.android.data.database.entity.UserEntity
import io.reactivex.Observable

import java.util.*


interface ClientRepository {

    fun initApp(
        onSignInState: () -> Unit,
        onSignedOutState: () -> Unit,
        onError: () -> Unit
    )

    fun getCurrentUser(): Observable<UserEntity>

    fun signIn(username: String, password: String, loggedIn: () -> Unit, unauthorized: () -> Unit, error: () -> Unit)


    suspend fun signOut()

    fun initWebSocket()
}