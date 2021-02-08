package co.tcc.koga.android.ui.splash_screen

import androidx.lifecycle.*
import co.tcc.koga.android.data.repository.ClientRepository

import io.reactivex.disposables.CompositeDisposable

import javax.inject.Inject

class SplashScreenViewModel @Inject constructor(private val repository: ClientRepository) :
    ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val _appStatus = MutableLiveData<Boolean>()
    private val _isLogged = MutableLiveData<Boolean>()
    val appStatus: LiveData<Boolean>
        get() = _appStatus
    val isLogged: LiveData<Boolean>
        get() = _isLogged

    fun initApp() {
        repository.initApp(
            fun() {
                getUser()
            },

            fun() {
                _isLogged.value = false
            },

            fun() {
                _appStatus.value = false
            })
    }


    private fun getUser() {
        val disposable = repository.getCurrentUser().subscribe(
            {
                _isLogged.value = true
            },
            {

            },
        )
        compositeDisposable.add(disposable)
    }
}