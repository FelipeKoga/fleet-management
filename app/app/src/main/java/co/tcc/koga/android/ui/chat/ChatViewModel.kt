package co.tcc.koga.android.ui.chat

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.lifecycle.*
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.socket.ChatActions
import co.tcc.koga.android.data.network.socket.MessageActions
import co.tcc.koga.android.data.network.socket.UserActions
import co.tcc.koga.android.data.repository.AudioRepository
import co.tcc.koga.android.data.repository.ChatsRepository
import co.tcc.koga.android.data.repository.ClientRepository
import co.tcc.koga.android.data.repository.MessageRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val chatsRepository: ChatsRepository,
    private val clientRepository: ClientRepository,
    private val audioRepository: AudioRepository,
    private val context: Context,
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
    private var file: File? = null
    private var recorder: MediaRecorder? = null

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

    fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC);
            setOutputFormat(AudioFormat.ENCODING_PCM_16BIT);
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            setAudioChannels(1);
            setAudioEncodingBitRate(128000);
            setAudioSamplingRate(48000);

            try {
                val fileName = "audio${UUID.randomUUID().toString()}"
                file = File.createTempFile(fileName, ".wav", context.cacheDir)
                setOutputFile(file?.absolutePath)
                prepare()
                start()
            } catch (e: IOException) {
                println(e)
                println("prepare() failed")
            }
        }
    }

    fun stopRecording() {
        recorder?.stop()
        recorder?.release()

        val size = file!!.length().toInt()
        println(size)
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }



        uploadAudio(bytes)

    }

    private fun uploadAudio(byteArray: ByteArray) = viewModelScope.launch {
        val key = "company/${clientRepository.user().companyId}/chat/${chat.value?.id}/${
            clientRepository.user().username
        }/audios/${Math.random()}.wav"

        withContext(Dispatchers.IO) {
            compositeDisposable.add(audioRepository.uploadUrl(key).subscribe { uploadResponse ->
                println(uploadResponse)
                audioRepository.uploadAudio(file as File, key).subscribe {
                    sendAudio(uploadResponse.getURL)
                }
            })
        }

    }


    private fun sendAudio(getURL: String) = viewModelScope.launch {
        val message = MessageEntity(
            chat.value?.id as String,
            getURL,
            clientRepository.user().username,
            "",
            true
        )

        println("NEW MESSAGE $message")
        repository.sendMessage(message)
        val chat = _chat.value
        chat?.messages?.add(message)
        _chat.postValue(chat)
        chatsRepository.updateChat(chat as ChatEntity)
    }

}