package co.tcc.koga.android.ui.auth.forgot_password

import java.io.Serializable

enum class ForgotPasswordStatus {
    PENDING,
    CODE_SENDED,
    SUCCESS,
    NOT_LONG_ENOUGH,
    INTERNAL_ERROR
}

data class RecoverPasswordForm(
    var username: String = "",
    var code: String = "",
    var newPassword: String = "",
    var confirmNewPassword: String = "",

    ) : Serializable