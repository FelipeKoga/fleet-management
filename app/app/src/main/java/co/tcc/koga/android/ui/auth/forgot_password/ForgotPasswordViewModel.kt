package co.tcc.koga.android.ui.auth.forgot_password

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.tcc.koga.android.R
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.ui.auth.ForgotPasswordStatus
import co.tcc.koga.android.ui.auth.RecoverPasswordForm
import javax.inject.Inject

class ForgotPasswordViewModel @Inject constructor(
    private val repository: ClientRepository,
    private val context: Context
) :
    ViewModel() {


    private val _isLoading = MutableLiveData(false)

    val isLoading: LiveData<Boolean> get() = _isLoading
    val formFields = RecoverPasswordForm()
    val formErrors = MutableLiveData<MutableMap<String, String>>(mutableMapOf())

    val passwordRecoverStatus = MutableLiveData(ForgotPasswordStatus.PENDING)

    private fun isFormValid(): Boolean {
        val errors = mutableMapOf<String, String>()
        formErrors.postValue(errors)

        if (formFields.username.isEmpty()) {
            errors["username"] = context.getString(R.string.required)
        }

        formErrors.postValue(errors)

        return errors.isEmpty()
    }

    fun sendCode() {
        if (!isFormValid()) return
        _isLoading.postValue(true)
        repository.sendCode(formFields.username, fun() {
            passwordRecoverStatus.postValue(ForgotPasswordStatus.CODE_SENDED)
            _isLoading.postValue(false)
        }, fun() {
            passwordRecoverStatus.postValue(ForgotPasswordStatus.INTERNAL_ERROR)
            _isLoading.postValue(false)

        })
    }

}