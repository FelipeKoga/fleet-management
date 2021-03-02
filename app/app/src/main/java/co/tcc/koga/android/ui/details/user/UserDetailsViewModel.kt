package co.tcc.koga.android.ui.details.user

import androidx.lifecycle.ViewModel
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.utils.Constants
import javax.inject.Inject

class UserDetailsViewModel @Inject constructor() : ViewModel() {


    fun getRole(user: UserEntity): String {
        return when(user.role) {
            Constants.UserRole.EMPLOYEE.name -> "Funcionário"
            Constants.UserRole.ADMIN.name -> "Administrador"
            Constants.UserRole.EMPLOYEE.name -> "Operador"
            else -> "Funcionário"
        }
    }
}