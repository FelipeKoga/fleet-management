package co.tcc.koga.android.ui.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.network.contactsService
import co.tcc.koga.android.domain.Contact
import kotlinx.coroutines.launch
import java.lang.Exception

class ContactsViewModel : ViewModel() {
    private val _contacts = MutableLiveData<Contact>()

    fun getContacts() {
        viewModelScope.launch {
            try {
                val series = contactsService.getContacts()
                println(series)
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}