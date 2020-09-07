package co.tcc.koga.android.data.network

import android.content.Context
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignInResult
import java.lang.Exception

class Client {

    fun initAWSClient(
        applicationContext: Context, onInitSuccess: (isSignIn: Boolean) -> Unit,
        onInitError: () -> Unit
    ) {
        AWSMobileClient.getInstance().initialize(applicationContext,
            object : Callback<UserStateDetails> {
                override fun onResult(result: UserStateDetails?) {
                    when (result?.userState) {
                        UserState.SIGNED_OUT -> onInitSuccess(false)
                        UserState.SIGNED_IN -> onInitSuccess(true)
                        else -> onInitError()
                    }
                }

                override fun onError(e: Exception?) {
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
                    println(result?.signInState)
                    onSignInSuccess()
                }

                override fun onError(e: Exception?) {
                    println(e)
                    onSignInError(e)
                }

            })
    }

    fun isSignIn(): Boolean {
        return AWSMobileClient.getInstance().isSignedIn
    }

    fun signOut() {
        AWSMobileClient.getInstance().signOut()
    }

    fun username(): String {
        return AWSMobileClient.getInstance().username
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