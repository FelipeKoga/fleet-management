package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "chat")
data class ChatEntity(
    @PrimaryKey
    val chatId: String,
    val groupName: String?,
    val avatar: String?,
    val createdAt: String?,
    val admin: String?,
    val isPrivate: Boolean,
    val members: List<UserEntity>?,
    val user: UserEntity?,
    val newMessages: Long,
    val lastMessage: MessageEntity? = null
) : Parcelable