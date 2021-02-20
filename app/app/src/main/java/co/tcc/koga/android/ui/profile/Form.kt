package co.tcc.koga.android.ui.profile

import co.tcc.koga.android.data.database.entity.UserEntity
import java.io.Serializable

data class ProfileForm(
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var customName: String? = "",
    ) : Serializable {
    fun init(user: UserEntity) {
        name = user.name
        email = user.email
        phone = user.phone
        customName = user.customName
    }
}