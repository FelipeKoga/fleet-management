package co.tcc.koga.android.ui.chats.chat

import android.media.AudioFormat

class PTTMetadata {
    val sampleRate = 8000
    var bufferSize = 1024
    val channel = AudioFormat.CHANNEL_IN_MONO
    val encoding = AudioFormat.ENCODING_PCM_FLOAT
}