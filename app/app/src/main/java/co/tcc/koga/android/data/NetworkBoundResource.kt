package co.tcc.koga.android.data

import co.tcc.koga.android.data.network.ApiErrorResponse
import co.tcc.koga.android.data.network.ApiResponse
import co.tcc.koga.android.data.network.ApiSuccessResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
inline fun <DB, REMOTE> networkBoundResource(
    crossinline fetchFromLocal: () -> Flow<DB>,
    crossinline shouldFetchFromRemote: (DB?) -> Boolean = { true },
    crossinline fetchFromRemote: () -> Flow<ApiResponse<REMOTE>>,
    crossinline processRemoteResponse: (response: ApiSuccessResponse<REMOTE>) -> Unit = { Unit },
    crossinline saveRemoteData: (REMOTE) -> Unit = { Unit },
    crossinline onFetchFailed: (errorBody: String?, statusCode: Int) -> Unit = { _: String?, _: Int -> Unit }
) = flow<Resource<DB>> {

    emit(Resource.loading(null))

    val localData = fetchFromLocal().first()


    println("Fetch from local")
    emit(Resource.localData(localData))

    if (shouldFetchFromRemote(localData)) {

        emit(Resource.loading(localData))

        fetchFromRemote().collect { apiResponse ->
            when (apiResponse) {
                is ApiSuccessResponse -> {
                    processRemoteResponse(apiResponse)
                    apiResponse.body?.let { saveRemoteData(it) }
                    emitAll(fetchFromLocal().map { dbData ->
                        Resource.success(dbData)
                    })
                }

                is ApiErrorResponse -> {
                    onFetchFailed(apiResponse.errorMessage, apiResponse.statusCode)
                    emitAll(fetchFromLocal().map {
                        Resource.error(
                            apiResponse.errorMessage,
                            it
                        )
                    })
                }
            }
        }
    } else {
        emitAll(fetchFromLocal().map { Resource.success(it) })
    }
}