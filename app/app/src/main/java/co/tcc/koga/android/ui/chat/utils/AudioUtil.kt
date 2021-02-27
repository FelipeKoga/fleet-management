package co.tcc.koga.android.ui.chat.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import java.util.*

class AudioUtil {
    private lateinit var mediaPlayer: MediaPlayer
    private var currentURL: String = ""
    private var isPlaying: Boolean = false


    fun start(url: String, onStarted: () -> Unit, onCompletion: () -> Unit) {
        if (currentURL == url) {
            this.isPlaying = true
            mediaPlayer.start()
            return
        }

        currentURL = url
        isPlaying = true
        println(url)
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepare()
                start()
                setOnPreparedListener {
                    this@AudioUtil.isPlaying = true
                    onStarted()
                }
                setOnCompletionListener {
                    onCompletion()
                    mediaPlayer.seekTo(0)
                    this@AudioUtil.isPlaying = false
                }
            }
        } catch(e: Exception) {
            println(e)
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