package co.tcc.koga.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.UserRepository
import co.tcc.koga.android.databinding.LocationUpdateLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val repository: ClientRepository,
    val userRepository: UserRepository
) : ViewModel() {

    var currentUser: UserEntity = repository.user()

    fun signOut() = viewModelScope.launch {
        try {
            repository.signOut()
        } catch (e: Exception) {
            println(e)
        }
    }

    fun updateLocation(locationUpdate: Int?) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            userRepository.updateUser(
                UserEntity(
                    currentUser.username,
                    currentUser.companyId,
                    currentUser.email,
                    currentUser.fullName,
                    currentUser.phone,
                    currentUser.avatar,
                    currentUser.avatarUrl,
                    currentUser.name,
                    currentUser.status,
                    locationUpdate
                )
            ).subscribe {
                currentUser = it
            }
        }

    }
}