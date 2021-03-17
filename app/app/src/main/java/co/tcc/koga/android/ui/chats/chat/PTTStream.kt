package co.tcc.koga.android.ui.chats.chat

import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.delay

class PTTStream {
    private val metadata = PTTMetadata()
    private lateinit var recorder: AudioRecord

    @Volatile
    private var hasStopped = false


    fun start(onReceive: (buffer: FloatArray) -> Unit) {
        hasStopped = false
        initRecorder()
        recorder.startRecording()
        println("START RECORDING")

        while (!hasStopped) {
            val data = FloatArray(metadata.bufferSize)
            recorder.read(data, 0, data.size, AudioRecord.READ_BLOCKING)
            onReceive(data)
            Thread.sleep(50)
        }
    }

    fun stop() {
        hasStopped = true
        recorder.stop()
    }


    private fun initRecorder() {
        println(metadata.toString())
        val min = AudioRecord.getMinBufferSize(
            metadata.sampleRate,
            metadata.channel,
            metadata.encoding
        )
        metadata.bufferSize = min
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC, metadata.sampleRate,
            metadata.channel, metadata.encoding, min
        )
    }
}