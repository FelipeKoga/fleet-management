package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "contact")
data class ContactEntity(
    @PrimaryKey
    @ColumnInfo(name = "userId") val id: String = "",
    val companyId: String,
    val name: String,
    val email: String
) : Parcelable