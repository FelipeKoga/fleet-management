package co.tcc.koga.android.data.network

import android.content.Context
import co.tcc.koga.android.data.database.entity.UserEntity
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.ForgotPasswordResult
import com.amazonaws.mobile.client.results.SignInResult
import java.lang.Exception

class Client {

    lateinit var currentUser: UserEntity

    fun initAWSClient(
        applicationContext: Context, onInitSuccess: (isSignIn: Boolean) -> Unit,
        onInitError: () -> Unit
    ) {
        AWSMobileClient.getInstance().initialize(applicationContext,
            object : Callback<UserStateDetails> {
                override fun onResult(result: UserStateDetails?) {
                    println(result?.userState)
                    when (result?.userState) {
                        UserState.SIGNED_OUT -> onInitSuccess(false)
                        UserState.SIGNED_IN -> {
                            onInitSuccess(true)
                        }
                        else -> onInitError()
                    }
                }

                override fun onError(e: Exception?) {
                    println(e)
                    onInitError()
                }

            })
    }

    fun signIn(
        username: String,
        password: String,
        onSignInSuccess: () -> Unit,
        onSignInError: (Exception?) -> Unit
    ) {
        AWSMobileClient.getInstance().signIn(
            username,
            password,
            null,
            object : Callback<SignInResult> {
                override fun onResult(result: SignInResult?) {
                    onSignInSuccess()
                }

                override fun onError(e: Exception?) {
                    println(e)
                    onSignInError(e)
                }

            })
    }

    fun signOut() {
        AWSMobileClient.getInstance().signOut()
    }

    fun username(): String {
        return AWSMobileClient.getInstance().username
    }

    fun sendCode(
        username: String,
        onSendCodeSuccess: () -> Unit,
        onSendCodeError: (e: Exception?) -> Unit
    ) {
        AWSMobileClient.getInstance()
            .forgotPassword(username, object : Callback<ForgotPasswordResult> {
                override fun onResult(result: ForgotPasswordResult?) {
                    onSendCodeSuccess()
                }

                override fun onError(e: Exception?) {
                    onSendCodeError(e)
                }

            })
    }

    fun confirmChangePassword(
        password: String,
        code: String,
        onChangePasswordSuccess: () -> Unit,
        onChangePasswordError: (exception: Exception?) -> Unit
    ) {
        AWSMobileClient.getInstance()
            .confirmForgotPassword(password, code, object : Callback<ForgotPasswordResult> {
                override fun onResult(result: ForgotPasswordResult?) {
                    onChangePasswordSuccess()
                }

                override fun onError(e: Exception?) {
                    onChangePasswordError(e)
                }

            })
    }


    companion object {
        private lateinit var instance: Client
        fun getInstance(): Client {
            if (!::instance.isInitialized) {
                instance = Client()
            }
            return instance
        }
    }
}