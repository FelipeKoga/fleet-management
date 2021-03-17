package co.tcc.koga.android.ui.chats.chat.utils

import android.media.AudioFormat
import org.json.JSONObject

class PushToTalkMetadata {
    private val DEFAULT_SAMPLE_RATE = 44100
    private val DEFAULT_CHANNELS = 2
    private val DEFAULT_ENCODING = 32
    private val DEFAULT_BUFFER_SIZE = 6144 * 4

    private var sampleRate = DEFAULT_SAMPLE_RATE
    private var bufferSize = DEFAULT_BUFFER_SIZE
    private var channels = DEFAULT_CHANNELS
    private var bytesPerSample = DEFAULT_ENCODING / 8
    private var bufferSizeInBytes = DEFAULT_BUFFER_SIZE * bytesPerSample


    fun getSampleRate(): Int {
        return sampleRate
    }

    fun getBufferSize(): Int {
        return bufferSize
    }

    fun getChannels(`in`: Boolean): Int {
        return if (channels == 1) {
            if (`in`) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_OUT_MONO
        } else if (channels == 2) {
            if (`in`) AudioFormat.CHANNEL_IN_STEREO else AudioFormat.CHANNEL_OUT_STEREO
        } else {
            0
        }
    }

    fun getEncoding(): Int {
        return AudioFormat.ENCODING_PCM_FLOAT
    }

    fun setBufferSize(size: Int) {
        bufferSize = size
        bufferSizeInBytes = bufferSize * bytesPerSample
    }


    override fun toString(): String {
        val data = JSONObject()
        val o = JSONObject()
        o.put("sampleRate", sampleRate)
        o.put("bufferSize", bufferSize)
        o.put("channels", channels)
        o.put("encoding", 32)
        data.putOpt("meta", o)
        return data.toString()
    }

}