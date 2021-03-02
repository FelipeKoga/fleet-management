package co.tcc.koga.android.ui.details.group

import androidx.lifecycle.ViewModel
import co.tcc.koga.android.data.repository.ClientRepository
import javax.inject.Inject

class GroupDetailsViewModel @Inject constructor(val clientRepository: ClientRepository): ViewModel() {
    val user = clientRepository.user()

}