package co.tcc.koga.android.ui.settings

import androidx.lifecycle.ViewModel
import co.tcc.koga.android.data.AWSClient

class SettingsViewModel : ViewModel() {


    fun signOut() {
        AWSClient.getInstance().signOut()
    }
}