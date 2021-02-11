package co.tcc.koga.android.ui.chat

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.socket.ChatActions
import co.tcc.koga.android.data.network.socket.MessageActions
import co.tcc.koga.android.data.network.socket.UserActions
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.MessageRepository
import io.reactivex.disposables.CompositeDisposable

import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val chatsRepository: ChatsRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val _chat = MutableLiveData<ChatEntity>()
    private val _messages = MutableLiveData<MutableList<MessageEntity?>>()
    private val _isLoading = MutableLiveData(false)
    private val _error = MutableLiveData(false)

    val chat: LiveData<ChatEntity> get() = _chat
    val messages: LiveData<MutableList<MessageEntity?>> get() = _messages
    val isLoading: LiveData<Boolean> get() = _isLoading
    val error: LiveData<Boolean> get() = _error

    val username = clientRepository.user().username

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun getMessages(chatId: String) {
        _isLoading.postValue(true)
        compositeDisposable.add(repository.getMessages(chatId).subscribe({ response ->
            _isLoading.postValue(false)
            _error.postValue(false)
            _messages.postValue(response.data)
        }, {
            _isLoading.postValue(false)
            _error.postValue(true)
        }))
    }


    private fun insertMessage(message: MessageEntity, chatId: String) = viewModelScope.launch {
        chatsRepository.openChat(chatId)
        repository.insertMessage(message)

    }

    fun observeMessageUpdates(chatId: String) = viewModelScope.launch {
        compositeDisposable.add(repository.observeMessageUpdated().subscribe { update ->

            if (update.action === MessageActions.NEW_MESSAGE) {
                insertMessage(update.body, chatId)
            }

            if (update.action === MessageActions.MESSAGE_SENT) {
                insertMessage(update.body, chatId)
            }
        })
    }

    fun observeChatUpdates(chat: ChatEntity) {
        _chat.postValue(chat)
        compositeDisposable.add(chatsRepository.observeChatUpdates().subscribe { update ->
            if (update.action == ChatActions.CHAT_UPDATED) {
                if (chat.id == update.body.id) {
                    _chat.postValue(update.body)
                }
            }

        })
    }

    fun observeUserUpdates() {
        compositeDisposable.add(chatsRepository.observeUserUpdates().subscribe { update ->
            if (
                (update.action === UserActions.USER_CONNECTED || update.action === UserActions.USER_DISCONNECTED)
                && chat.value !== null
                && chat.value?.user?.username == update.body.username
            ) {
                val newChat = chat.value?.copy()
                newChat?.user = update.body
                _chat.postValue(newChat)
            }
        })
    }

    fun openChat(chatId: String) = viewModelScope.launch {
        chatsRepository.openChat(chatId)
    }

    fun sendMessage(text: String, chatId: String) = viewModelScope.launch {
        try {
            val message = MessageEntity(chatId, text, clientRepository.user().username, "", false)
            repository.sendMessage(message)
            val chat = _chat.value
            chat?.messages?.add(message)
            _chat.postValue(chat)
            chatsRepository.updateChat(chat as ChatEntity)
        } catch (e: Exception) {
            println("ERROR SEND MESSAGES")
            println(e)
        }
    }

    fun playAudio(url: String) {
        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            prepare()
            start()
        }
    }

}