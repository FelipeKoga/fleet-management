package co.tcc.koga.android.ui.chats.chat.viewholder

import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowAudioMessageReceivedBinding
import co.tcc.koga.android.utils.FormatterUtil
import co.tcc.koga.android.utils.hide
import co.tcc.koga.android.utils.show

class RecipientAudioViewHolder(val binding: RowAudioMessageReceivedBinding) :
    RecyclerView.ViewHolder(binding.root), IAudioViewHolder {
    private var handler: Handler = Handler(binding.root.context.mainLooper)

    fun bind(
        item: MessageEntity,
        members: List<UserEntity>?,
        onPlayAudio: () -> Unit,
        onPauseAudio: () -> Unit,
        onSeekTo: (progress: Int) -> Unit
    ) {
        binding.run {
            if (!members.isNullOrEmpty()) {
                val user = members.find { item.username == it.username }
                textViewUserName.show()
                textViewUserName.text = user?.customName ?: user?.name
            }
            imageButtonPlayAudio.setOnClickListener {
                playAudio()
                onPlayAudio()
            }
            imageButtonPauseAudio.setOnClickListener {
                pauseAudio()
                onPauseAudio()
            }

            textViewMessageReceivedDuration.text = FormatterUtil.getDuration(item.duration ?: 0)
            textViewMessageReceivedHour.text = FormatterUtil.getHour(item.createdAt)

            setSeekBarMax(item.duration ?: 0)

            seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        seekBarAudio.progress = progress
                        onSeekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
        }
    }

    override fun playAudio() {
        binding.run {
            imageButtonPauseAudio.show()
            imageButtonPlayAudio.hide()
        }
    }

    override fun pauseAudio() {
        binding.run {
            imageButtonPauseAudio.hide()
            imageButtonPlayAudio.show()
        }
    }

    override fun stopAudio() {
        handler.post {
            run {
                binding.seekBarAudio.progress = 0
            }
        }
        pauseAudio()
    }

    override fun setProgress(progress: Int) {
        handler.post {
            run {
                binding.seekBarAudio.progress = progress
            }
        }
    }


    override fun setSeekBarMax(duration: Long) {
        val runnable = Runnable {
            run {
                binding.seekBarAudio.max = duration.toInt()
            }
        }
        handler.post(runnable)
    }

    override fun showLoading() {
        binding.run {
//            progressBarLoadingAudio.show()
            imageButtonPlayAudio.hide()
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
        ): RecipientAudioViewHolder {
            val binding = RowAudioMessageReceivedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return RecipientAudioViewHolder(binding)
        }
    }
}