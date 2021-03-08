package co.tcc.koga.android.ui.chats.new_group

import androidx.lifecycle.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class NewGroupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatsRepository: ChatsRepository,
    private val clientRepository: ClientRepository
) :
    ViewModel() {
    private val _chatCreated = MutableLiveData<ChatEntity>()
    private val _users = MutableLiveData<List<UserEntity>>()
    private val _selectedUsers = MutableLiveData<List<UserEntity>>()

    val users: LiveData<List<UserEntity>> get() = _users
    val selectedUsers: LiveData<List<UserEntity>> get() = _selectedUsers
    val chatCreated: LiveData<ChatEntity> get() = _chatCreated

    fun createChat(
        groupName: String,
        avatar: String,
    ) = viewModelScope.launch {
        try {
            if (!selectedUsers.value.isNullOrEmpty()) {
                val newChat =
                    chatsRepository.createGroup(selectedUsers.value!!, groupName, avatar)
                _chatCreated.postValue(newChat)
            }

        } catch (e: Exception) {
            println(e)
        }
    }

    fun getUsers() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            userRepository.getUsers().subscribe { users ->
                _users.postValue(users.filter { it.username != clientRepository.user().username })
            }
        }
    }


    fun handleUserSelected(user: UserEntity) {
        var users = _selectedUsers.value?.toMutableList()
        val found = users?.find { it.username == user.username }
        if (found != null) {
            users?.remove(user)
        } else {
            if (users.isNullOrEmpty()) {
                users = mutableListOf(user)
            } else {
                users.add(user)
            }
        }

        println(users)
        _selectedUsers.postValue(users)
    }
}