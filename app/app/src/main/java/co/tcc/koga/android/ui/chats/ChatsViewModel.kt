package co.tcc.koga.android.ui.chats

import androidx.lifecycle.*
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.MessageRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatsViewModel @Inject constructor(
    val repository: ChatsRepository,
) : ViewModel() {
    private val _chats = MutableLiveData<List<ChatEntity>>()

    val chats: LiveData<List<ChatEntity>> get() = _chats

    fun getAllChats() = repository.getChats().map {
        when (it.status) {
            Resource.Status.LOADING -> {
                Resource.loading(null)
            }
            Resource.Status.SUCCESS -> {
                _chats.postValue(it.data)
                Resource.success(it.data)
            }
            Resource.Status.LOCAL -> {
                _chats.postValue(it.data)
                Resource.localData(it.data)
            }
            Resource.Status.ERROR -> {
                Resource.error(it.message!!, null)
            }
        }
    }.asLiveData(viewModelScope.coroutineContext)


    fun messageReceived(): LiveData<MessageEntity> {
        return repository.messageReceived()
    }

    fun messageSent(): LiveData<MessageEntity> {
        return repository.messageSent()
    }

    fun handleNewMessage(messageEntity: MessageEntity, received: Boolean) = viewModelScope.launch {
        val chat = repository.updateChat(messageEntity, received)
        _chats.postValue(chats.value?.map {
            if (it.chatId === messageEntity.chatId) chat
            else it
        })
    }


}