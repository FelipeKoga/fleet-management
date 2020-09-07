package co.tcc.koga.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.data.network.websocket.Socket
import co.tcc.koga.android.data.repository.ClientRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(val repository: ClientRepository) : ViewModel() {


    fun signOut() = viewModelScope.launch {
        try {
            repository.signOut()
        } catch (e: Exception) {
            println(e)
        }


    }
}