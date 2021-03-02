package co.tcc.koga.android.data.network.aws

import android.content.Context
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.utils.Constants
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

    private val subject = PublishSubject.create<Constants.AuthStatus>()
    val subCurrentUser = PublishSubject.create<UserEntity>()

    lateinit var currentUser: UserEntity
    lateinit var tokenString: String

    fun initAWSClient(
        applicationContext: Context, onInitSuccess: (isSignIn: Boolean) -> Unit,
        onInitError: () -> Unit
    ) {
        AWSMobileClient.getInstance()
            .initialize(applicationContext,
                object : Callback<UserStateDetails> {
                    override fun onResult(result: UserStateDetails?) {
                        println(result?.userState)
                        when (result?.userState) {
                            UserState.SIGNED_OUT -> onInitSuccess(false)
                            UserState.SIGNED_IN -> {
                                tokenString =
                                    AWSMobileClient.getInstance().tokens.idToken.tokenString
                                subject.onNext(Constants.AuthStatus.LOGGED_IN)
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
                    subject.onNext(Constants.AuthStatus.LOGGED_IN)
                    tokenString = AWSMobileClient.getInstance().tokens.idToken.tokenString
                    onSignInSuccess()
                }

                override fun onError(e: Exception?) {
                    println(e)
                    onSignInError(e)
                }

            })
    }

    fun signOut() {
        subject.onNext(Constants.AuthStatus.LOGGED_OUT)
        AWSMobileClient.getInstance().signOut()
    }

    fun authStatus(): Observable<Constants.AuthStatus> {
        return subject.hide()
    }

    fun observeCurrentUser(): Observable<UserEntity> {
        return subCurrentUser.hide()
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


    fun getToken(): String = tokenString

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