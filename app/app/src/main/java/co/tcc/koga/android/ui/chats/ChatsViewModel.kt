package co.tcc.koga.android.ui.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.ContactEntity
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.ClientRepository
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class ChatsViewModel @Inject constructor(
    private val chatsRepository: ChatsRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {
    private val _currentUser = MutableLiveData<ContactEntity?>()
    private val _chats = MutableLiveData<List<ChatEntity>>()
    val chats: LiveData<List<ChatEntity>> get() = _chats
    val currentUser: LiveData<ContactEntity?>
        get() = _currentUser

    fun getCurrentUser() = viewModelScope.launch {
        _currentUser.postValue(clientRepository.getCurrentUserFromLocal())
        _currentUser.postValue(clientRepository.getCurrentUserFromRemote())
    }

    fun getChats() = viewModelScope.launch {
        _chats.postValue(chatsRepository.getAllChats())
        try {
            _chats.postValue(
                chatsRepository.getChatsFromNetwork(
                    _currentUser.value?.id as String,
                    _currentUser.value?.companyId as String
                )
            )
        } catch (e: Exception) {
            println(e)
        }
    }
}