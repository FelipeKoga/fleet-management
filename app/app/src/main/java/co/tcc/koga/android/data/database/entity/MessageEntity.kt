package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "message")
data class MessageEntity(
    var chatId: String,
    var message: String,
    var username: String,
    var status: String = "",
    var hasAudio: Boolean = false,
    var duration: Long? = null,
    var createdAt: Long = System.currentTimeMillis(),
    @PrimaryKey
    var messageId: String = UUID.randomUUID().toString()
) : Parcelable