package co.tcc.koga.android.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.PushToTalkRepository
import co.tcc.koga.android.utils.Constants
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: ClientRepository,
    private val lifecycleRegistry: LifecycleRegistry,
) :
    ViewModel() {
    private val _isSignIn = MutableLiveData(false)
    val isSignIn: LiveData<Boolean> get() = _isSignIn
    private var currentUser: UserEntity? = null

    fun observeUserStatus() = viewModelScope.launch {
        repository.observeCurrentUser().subscribe { user ->
            println("OBSERVEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE")
            currentUser = user
            println(user)
            if (user == null) {
                _isSignIn.postValue(false)
                lifecycleRegistry.onNext(Lifecycle.State.Stopped.WithReason(ShutdownReason.GRACEFUL))
            } else {
                _isSignIn.postValue(true)
                lifecycleRegistry.onNext(Lifecycle.State.Started)
            }
        }
    }



    fun isLocationEnabled(): Boolean {
        if (currentUser != null ) {
            return currentUser?.locationEnabled == true && currentUser?.role == Constants.UserRole.EMPLOYEE.name
        }
        return false
    }

    fun isSignIn(): Boolean = currentUser !== null

}