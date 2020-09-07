package co.tcc.koga.android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.data.network.websocket.Socket
import co.tcc.koga.android.data.repository.ClientRepository
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val repository: ClientRepository) :
    ViewModel() {
    private val _signInStatus = MutableLiveData<Boolean>()
    val loadingSignIn = MutableLiveData<Boolean>()
    val signInStatus: LiveData<Boolean> get() = _signInStatus

    fun authenticate(username: String, password: String) {
        println(username)
        println(password)
        loadingSignIn.value = true
        Client.getInstance().signIn(username, password, fun() {
            runOnUiThread {

                getCurrentUser()
            }
        }, fun(e: Exception?) {
            println(e)
            runOnUiThread {
                loadingSignIn.value = false
                _signInStatus.value = false
            }
        })
    }

    private fun getCurrentUser() = viewModelScope.launch {
        repository.getCurrentUserFromRemote()
        loadingSignIn.value = false
        _signInStatus.value = true
    }

}