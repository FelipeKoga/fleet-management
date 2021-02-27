package co.tcc.koga.android.ui.chats

import androidx.lifecycle.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.socket.ChatActions
import co.tcc.koga.android.data.network.socket.MessageActions
import co.tcc.koga.android.data.network.socket.UserActions
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.MessageRepository
import co.tcc.koga.android.utils.getUserAvatar
import io.reactivex.disposables.CompositeDisposable

import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatsViewModel @Inject constructor(
    val repository: ChatsRepository,
    private val messagesRepository: MessageRepository,
    private val clientRepository: ClientRepository,
) : ViewModel() {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val _chats = MutableLiveData<List<ChatEntity>>(mutableListOf())
    private val _isLoading = MutableLiveData(false)
    private val _error = MutableLiveData(false)

    val chats: LiveData<List<ChatEntity>> get() = _chats
    val isLoading: LiveData<Boolean> get() = _isLoading
    val error: LiveData<Boolean> get() = _error

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun getAllChats() {
        _isLoading.postValue(true)
        compositeDisposable.add(repository.getChats().subscribe({ response ->
            if (response.data.isNotEmpty() || !response.fromCache) {
                _isLoading.postValue(false)
                _error.postValue(false)
                _chats.postValue(response.data)
            }
        }, {
            _isLoading.postValue(false)
            _error.postValue(true)
        }))

    }

    fun getAvatar(): String {
        return if (clientRepository.user().avatarUrl != null) clientRepository.user().avatarUrl as String else getUserAvatar(
            clientRepository.user()
        )
    }

    fun observeChatUpdates() {
        compositeDisposable.add(repository.observeChatUpdates().subscribe { update ->
            if (update.action === ChatActions.CHAT_UPDATED) {
                val foundChat = _chats.value?.find { chat -> chat.id == update.body.id }
                if (foundChat !== null) {
                    val updateChat = update.body
                    if (foundChat.messages.size > 1) {
                        updateChat.messages = foundChat.messages
                    }
                    updateChat(update.body)
                }
            }

            if (update.action === ChatActions.CHAT_CREATED) {
                insertChat(update.body)
            }
        })
    }

    fun observeUserUpdates() {
        compositeDisposable.add(repository.observeUserUpdates().subscribe { update ->
            if (update.action === UserActions.USER_CONNECTED || update.action === UserActions.USER_DISCONNECTED) {
                _chats.postValue(_chats.value?.map { chat ->
                    if (chat.user?.username == update.body.username) {
                        val newChat = chat.copy()
                        newChat.user = update.body
                        updateChat(newChat)
                        newChat
                    } else {
                        chat
                    }
                })
            }
        })
    }

    fun observeMessageUpdates() {
        compositeDisposable.add(messagesRepository.observeMessageUpdated().subscribe { update ->
            if (update.action === MessageActions.NEW_MESSAGE) {
                val foundChat = _chats.value?.find { chat -> chat.id == update.body.chatId }
                    ?: return@subscribe
                val foundMessage =
                    foundChat.messages.find { message -> message?.messageId == update.body.messageId }
                if (foundMessage !== null) {
                    foundChat.messages.addAll(foundChat.messages.map { if (it?.messageId === update.body.messageId) update.body else it })
                } else {
                    foundChat.messages.add(update.body)
                }
                _chats.postValue(_chats.value?.map { chat ->
                    if (chat.id == foundChat.id) {
                        updateChat(foundChat)
                        foundChat
                    } else {
                        chat
                    }
                })
            }
        })
    }


    private fun insertChat(chat: ChatEntity) = viewModelScope.launch {
        repository.insertChat(chat)
    }

    private fun updateChat(chat: ChatEntity?) = viewModelScope.launch {
        if (chat !== null) {
            repository.updateChat(chat)
        }
    }
}