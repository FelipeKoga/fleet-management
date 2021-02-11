package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
@Entity(tableName = "message")
data class MessageEntity(
    val chatId: String,
    val message: String,
    val username: String,
    val status: String,
    val hasAudio: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    @PrimaryKey
    val messageId: String = UUID.randomUUID().toString()
) : Parcelable