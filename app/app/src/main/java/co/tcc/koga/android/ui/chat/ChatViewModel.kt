package co.tcc.koga.android.ui.chat

import androidx.lifecycle.*
import co.tcc.koga.android.data.Resource
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.MessageRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val chatsRepository: ChatsRepository
) : ViewModel() {
    private val _messages = MutableLiveData<List<MessageEntity>>()
    val messages: LiveData<List<MessageEntity>>
        get() = _messages

    fun messageReceived(): LiveData<MessageEntity> {
        return repository.messageReceived()
    }

    fun messageSent(): LiveData<MessageEntity> {
        return repository.messageSent()
    }

    fun openChat(chatId: String) = viewModelScope.launch {
        chatsRepository.openChat(chatId)
    }

    fun getMessages(chatId: String) = repository.getMessages(chatId).map {
        when (it.status) {
            Resource.Status.LOADING -> {
                Resource.loading(null)
            }
            Resource.Status.SUCCESS -> {
                _messages.value = it.data
                Resource.success(it.data)
            }
            Resource.Status.ERROR -> {
                Resource.error(it.message!!, null)
            }
            Resource.Status.LOCAL -> {
                _messages.value = it.data
                Resource.localData(it.data)
            }
        }
    }.asLiveData(viewModelScope.coroutineContext)

    fun sendMessage(text: String, chatId: String) = viewModelScope.launch {
        try {
            val msgs = _messages.value?.toMutableList()
            val message = MessageEntity(chatId, text, Client.getInstance().username())
            msgs?.add(message)
            _messages.postValue(msgs)
            repository.sendMessage(message)
        } catch (e: Exception) {
            println(e)
        }
    }

    fun handleNewMessage(chatId: String, message: MessageEntity, sent: Boolean) =
        viewModelScope.launch {
            try {
                if (chatId != message.chatId) return@launch
                val msgs = messages.value?.toMutableList()
                if (msgs !== null) {
                    val findMsg = msgs.find { it.messageId == message.messageId }
                    if (sent && findMsg != null) {
                        _messages.postValue(msgs.map {
                            if (it.messageId == message.messageId) {
                                message
                            } else {
                                it
                            }
                        })

                    } else {
                        msgs.add(message)
                        _messages.postValue(msgs)
                    }
                    repository.insertMessage(message)
                }
            } catch (e: Exception) {
                println(e)
            }
        }

}