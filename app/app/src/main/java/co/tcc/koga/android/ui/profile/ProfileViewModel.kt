package co.tcc.koga.android.ui.profile


import androidx.lifecycle.ViewModel
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.utils.getUserAvatar
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
) : ViewModel() {

    fun getAvatar(): String {
        return if (clientRepository.user().avatarUrl != null) clientRepository.user().avatarUrl as String else getUserAvatar(
            clientRepository.user()
        )
    }
}