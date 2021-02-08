package co.tcc.koga.android.data.network.aws

import android.content.Context
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.utils.AUTH_STATUS
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.ForgotPasswordResult
import com.amazonaws.mobile.client.results.SignInResult
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.lang.Exception

class Client {

    private val subject = PublishSubject.create<AUTH_STATUS>()

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
                            subject.onNext(AUTH_STATUS.LOGGED_IN)
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
                    subject.onNext(AUTH_STATUS.LOGGED_IN)
                    onSignInSuccess()
                }

                override fun onError(e: Exception?) {
                    println(e)
                    onSignInError(e)
                }

            })
    }

    fun signOut() {
        subject.onNext(AUTH_STATUS.LOGGED_OUT)
        AWSMobileClient.getInstance().signOut()
    }

    fun authStatus(): Observable<AUTH_STATUS> {
        return subject.hide()
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

    fun getToken(): String {
        return AWSMobileClient.getInstance().tokens.idToken.tokenString
    }


    companion object {
        private lateinit var instance: Client
        fun getInstance(): Client {
            if (!Companion::instance.isInitialized) {
                instance = Client()
            }
            return instance
        }
    }
}