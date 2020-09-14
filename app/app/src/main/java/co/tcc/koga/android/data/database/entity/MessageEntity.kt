package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.annotation.Nullable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "message")
data class MessageEntity(
    val chatId: String,
    val body: String,
    val sender: String,
    val sent: Boolean = false,
    @PrimaryKey
    val messageId: String = UUID.randomUUID().toString(),
    val createdAt: String = "${System.currentTimeMillis() / 1000}",
) : Parcelable