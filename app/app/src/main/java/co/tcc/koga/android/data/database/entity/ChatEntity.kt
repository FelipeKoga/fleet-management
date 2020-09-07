package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import co.tcc.koga.android.domain.Contact
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "chat")
data class ChatEntity(
    @PrimaryKey
    val id: String = "",
    @Embedded val user: ContactEntity?,
) : Parcelable