package co.tcc.koga.android.ui.login

import androidx.lifecycle.*
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.data.repository.ClientRepository
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import kotlinx.coroutines.flow.map
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

    fun initCurrentUser(): LiveData<Resource<UserEntity>> =
        repository.getCurrentUser().map {
            println(it.status)
            when (it.status) {
                Resource.Status.LOADING -> {
                    Resource.loading(null)
                }
                Resource.Status.SUCCESS -> {
                    repository.initWebSocket()
                    Resource.success(it.data)
                }
                Resource.Status.ERROR -> {

                    Resource.error(it.message!!, null)
                }
                Resource.Status.LOCAL -> {
                    Resource.localData(it.data)
                }
            }
        }.asLiveData(viewModelScope.coroutineContext)

    private fun getCurrentUser() = viewModelScope.launch {
        repository.getCurrentUser()
        repository.initWebSocket()

    }

}