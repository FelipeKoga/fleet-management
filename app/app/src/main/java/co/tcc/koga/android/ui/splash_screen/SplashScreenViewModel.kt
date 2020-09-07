package co.tcc.koga.android.ui.splash_screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.repository.ClientRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashScreenViewModel @Inject constructor(private val repository: ClientRepository) :
    ViewModel() {

    private val _appStatus = MutableLiveData<Boolean>()
    private val _isLogged = MutableLiveData<Boolean>()
    val appStatus: LiveData<Boolean>
        get() = _appStatus
    val isLogged: LiveData<Boolean>
        get() = _isLogged

    fun initApp() {
        repository.initApp(
            fun() {
                initCurrentUser()
            },

            fun() {
                _isLogged.value = false
            },

            fun() {
                _appStatus.value = false
            })
    }

    private fun initCurrentUser() = viewModelScope.launch {
        try {
            var user = repository.getCurrentUserFromLocal()
            if (user === null) {
                user = repository.getCurrentUserFromRemote()
            }
            repository.initWebSocket(user.id)
            _isLogged.value = true
        } catch (e: Exception) {
            println(e)
        }
    }

}