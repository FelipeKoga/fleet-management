package co.tcc.koga.android.ui.chats.chat.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

class PTTStream {
    object Metadata {
        var bufferSize = 1024
        const val sampleRate = 8000
        const val channel = AudioFormat.CHANNEL_IN_MONO
        const val encoding = AudioFormat.ENCODING_PCM_FLOAT
    }

    private var recorder: AudioRecord

    @Volatile
    private var isRecording = false

    init {
        val min = AudioRecord.getMinBufferSize(
            Metadata.sampleRate,
            Metadata.channel,
            Metadata.encoding
        )
        Metadata.bufferSize = min
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC, Metadata.sampleRate,
            Metadata.channel, Metadata.encoding, min
        )
    }

    fun start(onReceive: (audioAsString: String) -> Unit) {
        isRecording = false
        recorder.startRecording()
        Thread {
            while (!isRecording) {
                val data = FloatArray(Metadata.bufferSize)
                recorder.read(data, 0, data.size, AudioRecord.READ_BLOCKING)
                onReceive(data.joinToString())
            }
        }.start()
    }

    fun stop() {
        isRecording = true
        recorder.stop()
    }
}