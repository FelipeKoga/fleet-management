package co.tcc.koga.android.data.domain


abstract class BaseResponse<T>(
    val data: T,
    val fromCache: Boolean
)