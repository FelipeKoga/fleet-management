package co.tcc.koga.android.data.utils

import co.tcc.koga.android.data.network.ApiResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.awaitResponse
import java.lang.reflect.Type


class CallAdapter<T>(
    private val responseType: Type
) : CallAdapter<T, Flow<ApiResponse<T>>> {

    override fun responseType() = responseType

    @ExperimentalCoroutinesApi
    override fun adapt(call: Call<T>): Flow<ApiResponse<T>> = flow {

        val response = call.awaitResponse()
        emit(ApiResponse.create(response))

    }.catch { error ->
        emit(ApiResponse.create(error))
    }
}