package co.tcc.koga.android.ui.settings


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.UserRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val repository: ClientRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _isLoadingLocation = MutableLiveData(false)
    private val _isLoadingNotification = MutableLiveData(false)
    private val _isLoadingPushToTalk = MutableLiveData(false)

    var currentUser: UserEntity = repository.user()

    var location: Boolean = currentUser.locationEnabled
    var notification: Boolean = currentUser.notificationEnabled
    var pushToTalk: Boolean = currentUser.pushToTalkEnabled

    fun signOut() = viewModelScope.launch {
        try {
            repository.signOut()
        } catch (e: Exception) {
        }
    }


    private fun updateUser(newUser: UserEntity, _isLoading: MutableLiveData<Boolean>) =
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userRepository.updateUser(
                    newUser
                ).subscribe {
                    currentUser = it
                    _isLoading.postValue(false)
                }
            }
        }

    fun updateNotification(enabled: Boolean) {
        val newCurrentUser = currentUser
        newCurrentUser.notificationEnabled = enabled
        _isLoadingNotification.postValue(true)
        updateUser(newCurrentUser, _isLoadingNotification)
    }

    fun updatePushToTalk(enabled: Boolean) {
        val newCurrentUser = currentUser
        newCurrentUser.pushToTalkEnabled = enabled
        _isLoadingPushToTalk.postValue(true)
        updateUser(newCurrentUser, _isLoadingPushToTalk)
    }

    fun updateLocation(enabled: Boolean) = viewModelScope.launch {
        val newCurrentUser = currentUser
        newCurrentUser.locationEnabled = enabled
        _isLoadingLocation.postValue(true)
        updateUser(newCurrentUser, _isLoadingLocation)
    }
}