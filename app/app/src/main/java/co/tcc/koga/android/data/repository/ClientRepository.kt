package co.tcc.koga.android.data.repository

import co.tcc.koga.android.data.database.entity.ContactEntity


interface ClientRepository {

    fun initApp(
        onSignInState: () -> Unit,
        onSignedOutState: () -> Unit,
        onError: () -> Unit
    )

    suspend fun getCurrentUserFromLocal(): ContactEntity?

    suspend fun getCurrentUserFromRemote(): ContactEntity

    suspend fun signOut()

    fun initWebSocket(userId: String)
}