package co.tcc.koga.android.ui.chat

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.*
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.socket.MessageActions
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
    private val _messages = MutableLiveData<List<MessageEntity>>()
    val messages: LiveData<List<MessageEntity>>
        get() = _messages

    val username = clientRepository.user().username

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun getMessages(chatId: String) {
        compositeDisposable.add(repository.getMessages(chatId).subscribe({
            _messages.postValue(it)
        }, {
            println("ERROR GET MESSAGES")
            println(it)
        }))
    }


    private fun insertMessage(message: MessageEntity, chatId: String) =viewModelScope.launch {
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

    fun openChat(chatId: String) = viewModelScope.launch {
        chatsRepository.openChat(chatId)
    }

    fun sendMessage(text: String, chatId: String) = viewModelScope.launch {
        try {
            val message = MessageEntity(chatId, text, clientRepository.user().username, "", false)
            repository.sendMessage(message)
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