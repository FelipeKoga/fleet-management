package co.tcc.koga.android.ui.chats

import androidx.lifecycle.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.network.socket.ChatActions
import co.tcc.koga.android.data.network.socket.UserActions
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.ClientRepository
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ChatsViewModel @Inject constructor(
    val repository: ChatsRepository,
    private val clientRepository: ClientRepository,
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
        if (_chats.value === null) {
            _loadingChats.postValue(true)
            compositeDisposable.add(repository.getChats().subscribe({ chats ->
                _loadingChats.postValue(false)
                _chats.postValue(chats)
            }, { error ->
                println(error)
            }))
        }

    }

    fun getUserAvatar(): String? {
        return clientRepository.user().avatarUrl
    }

    fun observeChatUpdates() {
        repository.observeChatUpdates()
        compositeDisposable.add(repository.observeChatUpdates().subscribe { update ->
            if (update.action === ChatActions.CHAT_UPDATED) {
                repository.updateChat(update.body)
            }

        })
    }

    fun observeUserUpdates() {
        repository.observeChatUpdates()
        compositeDisposable.add(repository.observeUserUpdates().subscribe { update ->
            if (update.action === UserActions.USER_CONNECTED || update.action === UserActions.USER_DISCONNECTED) {
                val chat =
                    chats.value?.find { chat -> chat.user?.username == update.body.username }
                if (chat !== null) {
                    chat.user = update.body
                    repository.updateChat(chat)
                }

            }
        })
    }
}