package co.tcc.koga.android.ui.splash_screen

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.tcc.koga.android.data.AWSClient
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread

class SplashScreenViewModel : ViewModel() {
    private val _appStatus = MutableLiveData<Boolean>()
    val appStatus: LiveData<Boolean>
        get() = _appStatus

    fun initApp(applicationContext: Context) {
        AWSClient.getInstance().initAWSClient(applicationContext, fun() {
            runOnUiThread {
                _appStatus.value = true
            }
        }, fun(e: Exception?) {
            println(e)
            runOnUiThread {
                _appStatus.value = false
            }
        })
    }

}