package co.tcc.koga.android.ui.splash_screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.repository.ClientRepository
import kotlinx.coroutines.flow.map
import androidx.lifecycle.asLiveData
import co.tcc.koga.android.data.database.entity.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
                _isLogged.value = true
            },

            fun() {
                _isLogged.value = false
            },

            fun() {
                _appStatus.value = false
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
                    _appStatus.value = false
                    Resource.error(it.message!!, null)
                }
                Resource.Status.LOCAL -> {
                    Resource.localData(it.data)
                }
            }
        }.asLiveData(viewModelScope.coroutineContext)

}