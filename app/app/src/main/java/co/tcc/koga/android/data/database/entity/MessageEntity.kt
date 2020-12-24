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
    val timestamp: String? = "${System.currentTimeMillis() / 1000}",
    val status: String,
    @PrimaryKey
    val messageId: String
) : Parcelable