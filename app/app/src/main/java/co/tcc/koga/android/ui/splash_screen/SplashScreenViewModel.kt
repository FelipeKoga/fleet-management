package co.tcc.koga.android.ui.splash_screen

import androidx.lifecycle.*
import co.tcc.koga.android.data.repository.ClientRepository

import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

class SplashScreenViewModel @Inject constructor(private val repository: ClientRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow<SplashScreenUiState>(SplashScreenUiState.Pending)
    val uiState: StateFlow<SplashScreenUiState> get() = _uiState

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()


    fun initApp() = viewModelScope.launch {
        repository.initApp(
            fun() {
                getUser()
            },

            fun() {
                _uiState.value = SplashScreenUiState.LoggedOut
            },

            fun() {
                println("OPA")
                _uiState.value = SplashScreenUiState.Error
            })
    }


    private fun getUser() = viewModelScope.launch {
        val disposable = repository.getCurrentUser(false).subscribe(
            {
                println(repository.user())
                _uiState.value = SplashScreenUiState.LoggedIn
            },
            {
                println(it)
                _uiState.value = SplashScreenUiState.Error
            },
        )
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}