package co.tcc.koga.android.ui.auth.login

import android.content.Context
import androidx.lifecycle.*
import co.tcc.koga.android.R
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.utils.Constants
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import io.reactivex.disposables.CompositeDisposable
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

    private val _authenticationStatus = MutableLiveData<Constants.AuthStatus>()
    private val _isLoading = MutableLiveData(false)
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    val formFields = LoginForm()
    val authenticationStatus: LiveData<Constants.AuthStatus> get() = _authenticationStatus
    val isLoading: LiveData<Boolean> get() = _isLoading
    val formErrors = MutableLiveData<MutableMap<String, String>>(mutableMapOf())

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }


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
        _authenticationStatus.value = Constants.AuthStatus.PENDING
        _isLoading.postValue(true)
        repository.signIn(formFields.username, formFields.password, fun() {
            getUser()
        }, fun() {
            runOnUiThread {
                _isLoading.value = false
                _authenticationStatus.value = Constants.AuthStatus.UNAUTHORIZED
            }

        }, fun() {
            runOnUiThread {
                _isLoading.value = false
                _authenticationStatus.value = Constants.AuthStatus.ERROR
            }
        })
    }

    private fun getUser() = viewModelScope.launch {
        try {
            compositeDisposable.add(repository.getCurrentUser(true).subscribe {
                runOnUiThread {
                    _authenticationStatus.value = Constants.AuthStatus.LOGGED_IN
                }
            })
        } catch (e: Exception) {
        }
    }

}