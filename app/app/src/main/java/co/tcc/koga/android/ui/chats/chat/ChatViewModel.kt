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
import co.tcc.koga.android.ui.chats.chat.utils.PushToTalkMetadata
import kotlinx.coroutines.*
import java.io.*
import java.util.*
import javax.inject.Inject


class ChatViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val chatsRepository: ChatsRepository,
    private val clientRepository: ClientRepository,
    private val audioRepository: AudioRepository,
    private val context: Context, private val pushToTalkRepository: PushToTalkRepository,

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

    @Volatile
    var isRecording: Boolean = false

    val username = clientRepository.user().username
    private var file: File? = null
    private var recorder: MediaRecorder? = null
    private lateinit var pushToTalkRecorder: AudioRecord
    private var startRecorder: Long = System.currentTimeMillis()
    private var stopRecorder: Long = System.currentTimeMillis()
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
            println("sendAudio: ${audioMessageEntity}")

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

    var receiver = chat.value?.user?.username
    var receivers =
        if (chat.value?.members != null) chat.value?.members?.map { it.username } else null


    fun startPushToTalk() {
        _isRecordingPushToTalk.postValue(true)

        receiver = chat.value?.user?.username
        receivers =
            if (chat.value?.members != null) chat.value?.members?.map { it.username } else null

        initRecorder()

        isRecording = true
        pushToTalkRecorder.startRecording()
        pushToTalkRepository.start(chatId, receiver, receivers)
        Thread {
            while (isRecording) {
                val data = FloatArray(1024)
                pushToTalkRecorder.read(data, 0, data.size, AudioRecord.READ_BLOCKING)
                println(data.joinToString())
                pushToTalkRepository.send(chatId, receiver, receivers, data.joinToString(), 1024)
            }
        }.start()

    }


    fun stopPushToTalk() {
//        println("STOP PUSH TO TALK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        isRecording = false
        pushToTalkRecorder.stop()
        pushToTalkRepository.stop(chatId, receiver, receivers)
        _isRecordingPushToTalk.postValue(false)
    }

    private val metadata = PTTMetadata()

    private fun initRecorder() {
        val min = AudioRecord.getMinBufferSize(
            metadata.sampleRate,
            metadata.channel,
            metadata.encoding
        )
        metadata.bufferSize = min
        pushToTalkRecorder = AudioRecord(
            MediaRecorder.AudioSource.MIC, metadata.sampleRate,
            metadata.channel, metadata.encoding, min
        )
    }
}