package co.tcc.koga.android.ui.new_chat

import androidx.lifecycle.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.UserRepository
import co.tcc.koga.android.dto.UserDTO
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewChatViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatsRepository: ChatsRepository
) :
    ViewModel() {
    private val _chatCreated = MutableLiveData<ChatEntity>()
    private val _users = MutableLiveData<List<UserEntity>>()
    private val _selectUser = MutableLiveData<List<UserDTO>>()

    val users: LiveData<List<UserEntity>> get() = _users
    val selectUser: LiveData<List<UserDTO>> get() = _selectUser
    val chatCreated: LiveData<ChatEntity> get() = _chatCreated

//    fun getAllUsers() = userRepository.getUsers().map {
////        when (it.status) {
////            Resource.Status.SUCCESS -> {
////                _users.postValue(it.data)
////                Resource.success(it)
////            }
////            Resource.Status.ERROR -> Resource.error(it.message!!, null)
////
////            Resource.Status.LOCAL -> {
////                _users.postValue(it.data)
////                Resource.localData(it)
////            }
////            Resource.Status.LOADING -> Resource.loading(null)
////        }
//    }.asLiveData(viewModelScope.coroutineContext)

    fun createChat(
        member_username: String
    ) = viewModelScope.launch {
        try {
            val newChat = chatsRepository.createChat(member_username)
            println("New chat")
            println(newChat)
            _chatCreated.postValue(newChat)
        } catch (e: Exception) {
            println(e)
        }
    }


}