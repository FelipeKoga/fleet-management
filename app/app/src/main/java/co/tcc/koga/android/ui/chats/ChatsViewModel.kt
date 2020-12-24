package co.tcc.koga.android.ui.chats

import androidx.lifecycle.*
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.MessageRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatsViewModel @Inject constructor(
    val repository: ChatsRepository,
) : ViewModel() {
    private val _chats = MutableLiveData<List<ChatEntity>>()
    private val _loadingChats = MutableLiveData(false)
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    val chats: LiveData<List<ChatEntity>> get() = _chats
    val loadingChats: LiveData<Boolean> get() = _loadingChats

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun getAllChats() {
        compositeDisposable.add(repository.getChats().subscribe({ chats ->
            println("RECEIVED CHATS:")
            println(chats)
            _chats.postValue(chats)
        }, { error ->
            println(error)
        }))
    }


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