package co.tcc.koga.android.data.network.payload

import co.tcc.koga.android.data.database.entity.UserEntity
import java.io.Serializable

data class PushToTalkResponse(
    val chatId: String,
    val user: UserEntity,
    val inputData: String?,
) : Serializable