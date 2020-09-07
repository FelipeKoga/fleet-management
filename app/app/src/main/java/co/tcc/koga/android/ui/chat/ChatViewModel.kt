package co.tcc.koga.android.ui.chat

import androidx.lifecycle.*
import co.tcc.koga.android.data.database.entity.ContactEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.websocket.Socket
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.MessageRepository
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {
    private val _messages = MutableLiveData<List<MessageEntity>>()
    private val _currentUser = MutableLiveData<ContactEntity>()
    private val _loadingMessages = MutableLiveData(true)
    val messages: LiveData<List<MessageEntity>>
        get() = _messages
    val loadingMessages: LiveData<Boolean> get() = _loadingMessages
    val currentUser: LiveData<ContactEntity> get() = _currentUser

    fun receiveNewMessage(): LiveData<MessageEntity> {
        return repository.receiveMessage()
    }

    fun messageSent(): LiveData<MessageEntity> {
        return repository.messageSent()
    }

    fun getMessages(chatId: String) = viewModelScope.launch {
        try {
            val currentUser = clientRepository.getCurrentUserFromLocal()
            _currentUser.postValue(currentUser)
            var msgs = repository.getMessages(chatId)
            _messages.postValue(msgs)
            if (msgs.isNotEmpty()) _loadingMessages.value = false
            msgs = repository.getMessagesFromNetwork(
                currentUser!!.id,
                currentUser.companyId,
                chatId
            )
            _messages.postValue(msgs)
            _loadingMessages.value = false
        } catch (e: Exception) {
            println(e)
        }
    }

    fun sendMessage(text: String, recipientId: String, chatId: String) = viewModelScope.launch {
        try {
            val msgs = _messages.value?.toMutableList()
            msgs?.add(repository.sendMessage(text, _currentUser.value!!.id, recipientId, chatId))
            _messages.postValue(msgs)
        } catch (e: Exception) {
            println(e)
        }
    }

    fun handleNewMessage(message: MessageEntity, sent: Boolean) = viewModelScope.launch {
        try {
            val msgs = messages.value?.toMutableList()
            if (msgs !== null) {
                if (sent) {
                    repository.replaceMessage(message)
                    _messages.postValue(msgs.map {
                        if (it.id.isEmpty() && it.createdAt == message.createdAt) {
                            message
                        } else {
                            it
                        }
                    })
                } else {
                    msgs.add(message)
                    repository.insert(message)
                    _messages.postValue(msgs)
                }
            }
        } catch (e: Exception) {
            println(e)
        }
    }

}