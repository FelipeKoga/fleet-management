package co.tcc.koga.android.ui.chats.new_chat

import androidx.lifecycle.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class NewChatViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatsRepository: ChatsRepository,
    private val clientRepository: ClientRepository
) :
    ViewModel() {
    private val _isLoading = MutableLiveData(false)
    private val _chatCreated = MutableLiveData<ChatEntity>()
    private val _users = MutableLiveData<List<UserEntity>>()
    private val _filteredUsers = MutableLiveData<List<UserEntity>>()

    val isLoading: LiveData<Boolean> get() = _isLoading
    val users: LiveData<List<UserEntity>> get() = _users
    val filteredUsers: LiveData<List<UserEntity>> get() = _filteredUsers

    val chatCreated: LiveData<ChatEntity> get() = _chatCreated

    fun getUsers() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            userRepository.getUsers().subscribe { users ->
                _users.postValue(users.filter { it.username != clientRepository.user().username })
            }
        }
    }

    fun filterUsers(name: String) {
        _filteredUsers.postValue(users.value?.filter {
            it.name.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT))
        })
    }

    fun clearFilter() {
        _filteredUsers.postValue(listOf())
        _users.postValue(_users.value)
    }

    fun createChat(
        member_username: String
    ) = viewModelScope.launch {
        try {
            val newChat = chatsRepository.createChat(member_username)
            _chatCreated.postValue(newChat)
        } catch (e: Exception) {
            println(e)
        }
    }
}