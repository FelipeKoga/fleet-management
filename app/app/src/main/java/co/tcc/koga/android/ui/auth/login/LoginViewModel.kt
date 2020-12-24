package co.tcc.koga.android.ui.auth.login

import android.content.Context
import androidx.lifecycle.*
import co.tcc.koga.android.R
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.utils.AUTH_STATUS
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import kotlinx.coroutines.launch
import java.io.Serializable
import java.lang.Exception
import javax.inject.Inject


class LoginViewModel @Inject constructor(
    private val repository: ClientRepository,
    private val context: Context
) :
    ViewModel() {
    data class LoginForm(
        var username: String = "",
        var password: String = "",
    ) : Serializable

    private val _authenticationStatus = MutableLiveData<AUTH_STATUS>()
    private val _isLoading = MutableLiveData(false)

    val formFields = LoginForm()
    val authenticationStatus: LiveData<AUTH_STATUS> get() = _authenticationStatus
    val isLoading: LiveData<Boolean> get() = _isLoading
    val formErrors = MutableLiveData<MutableMap<String, String>>(mutableMapOf())

    private fun isFormValid(): Boolean {
        val errors = mutableMapOf<String, String>()
        formErrors.postValue(errors)

        if (formFields.username.isEmpty()) {
            errors["username"] = context.getString(R.string.required)
        }

        if (formFields.password.isEmpty()) {
            errors["password"] = context.getString(R.string.required)
        }

        formErrors.postValue(errors)

        return errors.isEmpty()
    }

    fun authenticate() {
        if (!isFormValid()) return
        _authenticationStatus.value = AUTH_STATUS.PENDING
        _isLoading.postValue(true)
        repository.signIn(formFields.username, formFields.password, fun() {
            getUser()
        }, fun() {
            runOnUiThread {
                _isLoading.value = false
                _authenticationStatus.value = AUTH_STATUS.UNAUTHORIZED
            }

        }, fun() {
            runOnUiThread {
                _isLoading.value = false
                _authenticationStatus.value = AUTH_STATUS.ERROR
            }
        })
    }

    private fun getUser() = viewModelScope.launch {
        try {
            val user = repository.getCurrentUser()
            println(user)
        } catch (e: Exception) {
            println(e)
        }

    }

}