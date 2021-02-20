package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val companyId: String,
    val email: String = "",
    val fullName: String? = "",
    val phone: String? = "",
    val avatar: String? = "",
    val avatarUrl: String? = "",
    val name: String? = "",
    val status: String = "OFFLINE",
    val locationUpdate: Int? = null,
    val color: String = "",
    val role: String = "EMPLOYEE",
    var notificationEnabled: Boolean = false,
    var pushToTalkEnabled: Boolean = false,
    var locationEnabled: Boolean = false
) : Parcelable