package co.tcc.koga.android.ui.chats.new_group

import androidx.lifecycle.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject


class NewGroupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatsRepository: ChatsRepository
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
        val users = userRepository.getLocalUsers()

    }

    fun handleSelectedUser(user: UserEntity) {
        val users = _selectedUsers.value?.toMutableList()
//        if (user.isSelected) {
//            if (users.isNullOrEmpty()) {
//                _selectedUsers.postValue(listOf(user))
//            } else {
//                users.add(user)
//                _selectedUsers.postValue(users)
//            }
//        } else {
//            if (users.isNullOrEmpty()) _selectedUsers.postValue(listOf())
//            else
//                _selectedUsers.postValue(users.filter {
//                    it.username !== user.username
//                })
//        }

        _users.postValue(_users.value?.map { if (user.username === it.username) user else it })
    }
}