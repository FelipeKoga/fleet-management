package co.tcc.koga.android.ui.chats.chat

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.socket.ChatActions
import co.tcc.koga.android.data.network.socket.MessageActions
import co.tcc.koga.android.data.network.socket.UserActions
import co.tcc.koga.android.data.repository.*
import co.tcc.koga.android.ui.chats.chat.utils.PTTStream
import kotlinx.coroutines.*
import java.io.*
import java.util.*
import javax.inject.Inject


class ChatViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val chatsRepository: ChatsRepository,
    private val clientRepository: ClientRepository,
    private val audioRepository: AudioRepository,
    private val context: Context,
    private val pushToTalkRepository: PushToTalkRepository,

    ) : ViewModel() {
    private val _chat = MutableLiveData<ChatEntity>()
    private val _messages = MutableLiveData<MutableList<MessageEntity?>>()
    private val _isLoading = MutableLiveData(false)
    private val _error = MutableLiveData(false)
    private val _isRecordingPushToTalk = MutableLiveData(false)

    val chat: LiveData<ChatEntity> get() = _chat
    val messages: LiveData<MutableList<MessageEntity?>> get() = _messages
    val isLoading: LiveData<Boolean> get() = _isLoading
    val error: LiveData<Boolean> get() = _error
    val isRecordingPushToTalk: LiveData<Boolean> get() = _isRecordingPushToTalk

    val username = clientRepository.user().username
    private var file: File? = null
    private var recorder: MediaRecorder? = null
    private var startRecorder: Long = System.currentTimeMillis()
    lateinit var chatId: String


    fun getMessages() = viewModelScope.launch {
        _isLoading.postValue(true)
        repository.getMessages(chatId).subscribe({ response ->
            _isLoading.postValue(false)
            _error.postValue(false)
            _messages.postValue(response.data)
        }, {
            _isLoading.postValue(false)
            _error.postValue(true)
        })
    }


    private fun insertMessage(message: MessageEntity) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            chatsRepository.openChat(chatId)
            repository.insertMessage(message)
        }
    }

    fun observeRecordingPTT() = viewModelScope.launch {
        pushToTalkRepository.isRecording().subscribe {
            _isRecordingPushToTalk.postValue(it)
        }
    }

    fun observeMessageUpdates() = viewModelScope.launch {
        repository.observeMessageUpdated().subscribe { update ->
            if (update.action === MessageActions.NEW_MESSAGE) {
                insertMessage(update.body)
            }

            if (update.action === MessageActions.MESSAGE_SENT) {
                insertMessage(update.body)
            }
        }
    }

    fun observeChatUpdates(chat: ChatEntity) = viewModelScope.launch {
        _chat.postValue(chat)
        chatsRepository.observeChatUpdates().subscribe { update ->
            if (update.action == ChatActions.CHAT_UPDATED) {
                if (chat.id == update.body.id) {
                    _chat.postValue(update.body)
                }
            }
        }
    }

    fun observeUserUpdates() = viewModelScope.launch {
        chatsRepository.observeUserUpdates().subscribe { update ->
            if (
                (update.action === UserActions.USER_CONNECTED || update.action === UserActions.USER_DISCONNECTED)
                && chat.value !== null
                && chat.value?.user?.username == update.body.username
            ) {
                val newChat = chat.value?.copy()
                newChat?.user = update.body
                _chat.postValue(newChat)
            }
        }
    }

    fun openChat() = viewModelScope.launch {
        chatsRepository.openChat(chatId)
    }

    fun sendMessage(text: String, chatId: String) = viewModelScope.launch {
        try {
            val message = MessageEntity(chatId, text, clientRepository.user().username)
            insertMessage(message)
            repository.sendMessage(message)
//            chatsRepository.updateChat(chat.value as ChatEntity)
        } catch (e: Exception) {
        }
    }


    fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(AudioFormat.ENCODING_PCM_16BIT)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                val fileName = "audio${UUID.randomUUID()}"
                file = File.createTempFile(fileName, ".wav", context.cacheDir)
                setOutputFile(file?.absolutePath)
                prepare()
                start()
                startRecorder = System.currentTimeMillis()
            } catch (e: IOException) {
            }
        }

    }

    fun stopRecording() {
        recorder?.stop()
        recorder?.release()
        val audioMessageEntity = getNewAudioMessage()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertMessage(audioMessageEntity)
            }
        }

        if (file == null) return
        val size = file!!.length().toInt()
        val bytes = ByteArray(size)
        try {
            val uri = Uri.parse(file?.absolutePath)
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            val millSecond = Integer.parseInt(durationStr)
            audioMessageEntity.duration = millSecond.toLong()
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        uploadAudio(audioMessageEntity)
    }

    private fun uploadAudio(audioMessageEntity: MessageEntity) = viewModelScope.launch {
        val key = "company/${clientRepository.user().companyId}/chat/${chat.value?.id}/${
            clientRepository.user().username
        }/audios/${Math.random()}.wav"

        withContext(Dispatchers.IO) {
            repository.insertMessage(audioMessageEntity)
            audioRepository.uploadUrl(key).subscribe { uploadResponse ->
                audioRepository.uploadAudio(file as File, uploadResponse.putURL).subscribe {
                    sendAudio(key, audioMessageEntity)
                }
            }
        }
    }

    private fun sendAudio(getURL: String, audioMessageEntity: MessageEntity) =
        viewModelScope.launch {
            val chat = _chat.value
            audioMessageEntity.message = getURL
            repository.sendMessage(audioMessageEntity)
            if (chat != null) {
                chatsRepository.updateChat(chat)
            }
        }

    private fun getNewAudioMessage(): MessageEntity {
        return MessageEntity(
            chatId,
            "",
            clientRepository.user().username,
            "",
            hasAudio = true
        )
    }

	private fun getReceivers(): List<String>? {
        val chat = chat.value!!
        return if (chat.members != null) chat.members?.map { member -> member.username } else listOf(
                chat.user?.username as String
            )
	}

    private val pttStream = PTTStream()

    fun startPushToTalk() {
        val receivers = getReceivers()
        pushToTalkRepository.start(chatId, receivers)
        pttStream.start { audioAsString ->
            pushToTalkRepository.send(audioAsString)
        }
    }

    fun stopPushToTalk() {
        pttStream.stop()
        pushToTalkRepository.stop()
        _isRecordingPushToTalk.postValue(false)
    }
}