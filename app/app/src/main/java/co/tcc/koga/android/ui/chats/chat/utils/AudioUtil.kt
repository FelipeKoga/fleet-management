package co.tcc.koga.android.ui.chats.chat.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import java.util.*

class AudioUtil {
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var currentURL: String = ""
    private var isPlaying: Boolean = false


    fun start(url: String, context: Context,  onStarted: () -> Unit, onCompletion: () -> Unit) {
        if (currentURL == url) {
            this.isPlaying = true
            mediaPlayer.start()
            onStarted()
            return
        }

        currentURL = url
        isPlaying = true
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)

                        .build()
                )
                setDataSource(context, Uri.parse(url))
                prepare()
                setOnPreparedListener {
                    start()
                    this@AudioUtil.isPlaying = true
                    onStarted()
                }
                setOnCompletionListener {
                    onCompletion()
                    mediaPlayer.seekTo(0)
                    this@AudioUtil.isPlaying = false
                }
            }
        } catch (e: Exception) {
        }

    }

    fun stop() {
        currentURL = ""
        mediaPlayer.stop()
    }

    fun pause() {
        mediaPlayer.pause()
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun seekTo(progress: Int) {
        mediaPlayer.seekTo(progress)
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }
}