package co.tcc.koga.android.data

import android.content.Context
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignInResult
import java.lang.Exception

class AWSClient {

    fun initAWSClient(
        applicationContext: Context, onInitSuccess: () -> Unit,
        onInitError: (Exception?) -> Unit
    ) {
        AWSMobileClient.getInstance().initialize(applicationContext,
            object : Callback<UserStateDetails> {
                override fun onResult(result: UserStateDetails?) {
                    println("ON RESULT")
                    println(result?.userState)
                    when(result?.userState) {
                        UserState.SIGNED_OUT -> onInitError(null)
                        UserState.SIGNED_IN -> onInitSuccess()
                        else -> onInitSuccess()
                    }
                }

                override fun onError(e: Exception?) {
                    onInitError(e)
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

    companion object {
        private var instance: AWSClient? = null
        fun getInstance(): AWSClient {
            if (instance === null) {
                instance = AWSClient()
            }
            return instance as AWSClient
        }
    }
}