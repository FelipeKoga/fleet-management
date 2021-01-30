package co.tcc.koga.android.ui.auth.confirm_forgot_password

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.tcc.koga.android.R
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.ui.auth.forgot_password.ForgotPasswordStatus
import co.tcc.koga.android.ui.auth.forgot_password.RecoverPasswordForm
import javax.inject.Inject

class ConfirmForgotPasswordViewModel @Inject constructor(
    private val repository: ClientRepository,
    private val context: Context
) :
    ViewModel() {
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    val formFields = RecoverPasswordForm()
    val formErrors = MutableLiveData<MutableMap<String, String>>(mutableMapOf())

    val passwordRecoverStatus = MutableLiveData(ForgotPasswordStatus.PENDING)
    var username: String = ""

    private fun isFormValid(): Boolean {
        val errors = mutableMapOf<String, String>()
        formErrors.postValue(errors)

        if (formFields.code.isEmpty()) {
            errors["code"] = context.getString(R.string.required)
        }

        if (formFields.newPassword.isEmpty()) {
            errors["newPassword"] = context.getString(R.string.required)
        }

        if (formFields.confirmNewPassword.isEmpty()) {
            errors["confirmNewPassword"] = context.getString(R.string.required)
        }

        if (formFields.newPassword.isNotEmpty() && formFields.confirmNewPassword.isNotEmpty() && formFields.newPassword != formFields.confirmNewPassword) {
            errors["differentPasswords"] = context.getString(R.string.differentPasswords)
        }

        formErrors.postValue(errors)

        println(formErrors.value)
        return errors.isEmpty()
    }


    fun changePassword() {
        if (!isFormValid()) return

        _isLoading.postValue(true)
        repository.confirmChangePassword(formFields.newPassword, formFields.code, fun() {
            repository.signIn(username, formFields.newPassword, fun() {
                passwordRecoverStatus.postValue(ForgotPasswordStatus.SUCCESS)
                _isLoading.postValue(false)
            }, fun() {
                passwordRecoverStatus.postValue(ForgotPasswordStatus.INTERNAL_ERROR)
                _isLoading.postValue(false)
            }, fun() {
                passwordRecoverStatus.postValue(ForgotPasswordStatus.INTERNAL_ERROR)
                _isLoading.postValue(false)
            })
        }, fun() {
            passwordRecoverStatus.postValue(ForgotPasswordStatus.NOT_LONG_ENOUGH)
            _isLoading.postValue(false)
        }, fun() {
            passwordRecoverStatus.postValue(ForgotPasswordStatus.INTERNAL_ERROR)
            _isLoading.postValue(false)
        })
    }
}