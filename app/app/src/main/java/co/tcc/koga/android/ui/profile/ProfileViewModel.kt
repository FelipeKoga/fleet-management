package co.tcc.koga.android.ui.profile


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.R
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.UserRepository
import co.tcc.koga.android.ui.auth.login.LoginViewModel
import co.tcc.koga.android.utils.UserRole
import co.tcc.koga.android.utils.getUserAvatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val userRepository: UserRepository,
    val context: Context
) : ViewModel() {
    var formFields: ProfileForm = ProfileForm()
    val formErrors = MutableLiveData<MutableMap<String, String>>(mutableMapOf())
    private val _isLoading = MutableLiveData(false)

    private val currentUser = clientRepository.user()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        formFields.init(currentUser)
    }

    fun getAvatar(): String {
        return currentUser.avatarUrl ?: getUserAvatar(
            currentUser
        )
    }

    fun getRole(): String {
        return when(currentUser.role) {
            UserRole.EMPLOYEE.name -> "Funcionário"
            UserRole.ADMIN.name -> "Administrador"
            UserRole.EMPLOYEE.name -> "Operador"
            else -> "Funcionário"
        }
    }

    private fun isFormValid(): Boolean {
        val errors = mutableMapOf<String, String>()
        formErrors.postValue(errors)

        if (formFields.name.isEmpty()) {
            errors["name"] = context.getString(R.string.required)
        }

        if (formFields.email.isEmpty()) {
            errors["email"] = context.getString(R.string.required)
        }

        if (formFields.phone.isEmpty()) {
            errors["phone"] = context.getString(R.string.required)
        }

        formErrors.postValue(errors)

        return errors.isEmpty()
    }

    fun save() = viewModelScope.launch {
        if (isFormValid()) {
            val newCurrentUser = currentUser
            newCurrentUser.apply {
                name = formFields.name
                phone = formFields.phone
                email = formFields.email
                customName = formFields.customName
            }
            _isLoading.postValue(true)
            withContext(Dispatchers.IO) {
                userRepository.updateUser(newCurrentUser).subscribe {
                    _isLoading.postValue(false)
                }
            }
        }
    }
}