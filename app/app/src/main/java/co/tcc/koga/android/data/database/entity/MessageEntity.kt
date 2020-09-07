package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "message")
data class MessageEntity(
    @PrimaryKey
    val id: String = "",
    val message: String,
    val senderId: String,
    val recipientId: String,
    val chatId: String,
    val createdAt: String = "${System.currentTimeMillis() / 1000}"
) : Parcelable