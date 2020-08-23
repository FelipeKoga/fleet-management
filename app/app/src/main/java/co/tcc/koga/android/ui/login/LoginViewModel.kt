package co.tcc.koga.android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.AWSClient
import co.tcc.koga.android.data.network.contactsService
import co.tcc.koga.android.domain.Contact
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import kotlinx.coroutines.launch
import java.lang.Exception

class LoginViewModel : ViewModel() {
    private val _signInStatus = MutableLiveData<Boolean>()
    val loadingSignIn = MutableLiveData<Boolean>()
    val signInStatus: LiveData<Boolean> get() = _signInStatus

    fun authenticate(username: String, password: String) {
        println(username)
        println(password)
        loadingSignIn.value = true
        AWSClient.getInstance().signIn(username, password, fun() {
            runOnUiThread {
                loadingSignIn.value = false
                _signInStatus.value = true
            }
        }, fun(e: Exception?) {
            println(e)
            runOnUiThread {
                loadingSignIn.value = false
                _signInStatus.value = false
            }
        })
    }

    private val _contacts = MutableLiveData<Contact>()

    fun getContacts() {
        viewModelScope.launch {
            try {
                val series = contactsService.getContacts()
                println(series)
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}