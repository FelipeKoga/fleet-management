package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    var username: String,
    var companyId: String,
    var email: String = "",
    var fullName: String? = "",
    var phone: String = "",
    var avatar: String? = "",
    var name: String = "",
    var customName: String? = "",
    var status: String = "OFFLINE",
    var locationUpdate: Int? = null,
    var color: String = "",
    var role: String = "EMPLOYEE",
    var notificationEnabled: Boolean = false,
    var pushToTalkEnabled: Boolean = false,
    var locationEnabled: Boolean = false
) : Parcelable