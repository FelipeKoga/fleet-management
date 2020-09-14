package co.tcc.koga.android.data.repository


import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow


interface ClientRepository {

    fun initApp(
        onSignInState: () -> Unit,
        onSignedOutState: () -> Unit,
        onError: () -> Unit
    )

    fun getCurrentUser(): Flow<Resource<UserEntity>>

    suspend fun signOut()

    fun initWebSocket()
}