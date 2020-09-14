package co.tcc.koga.android.data.network.payload

import co.tcc.koga.android.data.database.entity.UserEntity
import java.io.Serializable

data class NewChatPayload(
    val member_username: String?,
    val isPrivate: Boolean = true,
    val groupName: String = "",
    val avatar: String = "",
    val admin: String = "",
    val members: List<UserEntity> = listOf()
): Serializable