package co.tcc.koga.android.data.repository.impl

import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.dao.UserDAO
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.data.network.Service
import co.tcc.koga.android.data.networkBoundResource
import co.tcc.koga.android.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDAO,
    private val apiService: Service,
): UserRepository {

    @ExperimentalCoroutinesApi
    override fun getUsers(): Flow<Resource<List<UserEntity>>> {
        val user = Client.getInstance().currentUser
        return networkBoundResource(
            fetchFromLocal = { userDao.getAll(user.companyId, user.username) },
            shouldFetchFromRemote = {
                true
            },
            fetchFromRemote = {
                apiService.getUsers(user.companyId)
            },
            processRemoteResponse = { },
            saveRemoteData = {
                userDao.insertAll(it)
            },
            onFetchFailed = { e, a-> println(e)
            println(a)}
        ).flowOn(Dispatchers.IO)
    }

    override suspend fun getLocalUsers(): List<UserEntity> {
        val user = Client.getInstance().currentUser
        return userDao.suspendGetAll(user.companyId, user.username)
    }
}