package co.tcc.koga.android.ui.chats.chat

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.ui.chats.chat.utils.AudioUtil
import co.tcc.koga.android.ui.chats.chat.utils.MessageType
import co.tcc.koga.android.ui.chats.chat.viewholder.*
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils


class MessageAdapter(
    val username: String, val members:
    List<UserEntity>?,
    val context: Context
) :
    ListAdapter<MessageEntity, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
    private val audioUtil = AudioUtil()
    private var currentHolder: IAudioViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MessageType.SENDER_TEXT -> SenderViewHolder.create(parent)
            MessageType.RECIPIENT_TEXT -> RecipientViewHolder.create(parent)
            MessageType.SENDER_AUDIO -> SenderAudioViewHolder.create(parent)
            MessageType.RECIPIENT_AUDIO -> RecipientAudioViewHolder.create(parent)
            MessageType.SYSTEM -> SystemViewHolder.create(parent)
            else -> SenderViewHolder.create(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        if (holder is SystemViewHolder) {
            holder.bind(item)
        }

        if (holder is SenderViewHolder) {
            holder.bind(item)
        }

        if (holder is RecipientViewHolder) {
            holder.bind(item, members)
        }

        if (holder is SenderAudioViewHolder) {
            holder.bind(
                item,
                { play(item, holder) },
                { pause() },
                { progress -> seekTo(progress, holder) })
        }

        if (holder is RecipientAudioViewHolder) {
            holder.bind(
                item,
                members,
                { play(item, holder) },
                { pause() },
                { progress -> seekTo(progress, holder) })
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)

        if (message.username == "SYSTEM") return MessageType.SYSTEM

        if (message?.username == username) {
            if (message.hasAudio) return MessageType.SENDER_AUDIO
            return MessageType.SENDER_TEXT
        }

        if (message!!.hasAudio)
            return MessageType.RECIPIENT_AUDIO
        return MessageType.RECIPIENT_TEXT
    }

    private fun play(messageEntity: MessageEntity, holder: IAudioViewHolder) {
        if (currentHolder != holder && currentHolder != null) {
            currentHolder?.stopAudio()
            audioUtil.stop()
        }

        currentHolder = holder
        audioUtil.start(messageEntity.message, context, {
            currentHolder?.playAudio()

            Thread {
                while (audioUtil.isPlaying()) {
                    Thread.sleep(1)
                    ThreadUtils.runOnUiThread {
                        currentHolder?.setProgress(audioUtil.getCurrentPosition())
                    }
                }
            }.start()
        }) {
            currentHolder?.stopAudio()
            currentHolder = null
        }

    }

    private fun pause() {
        audioUtil.pause()
    }

    private fun seekTo(progress: Int, holder: IAudioViewHolder) {
        if (currentHolder == holder) {
            audioUtil.seekTo(progress)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessageEntity>() {
            override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
                return oldItem.messageId == newItem.messageId
            }

            override fun areContentsTheSame(
                oldItem: MessageEntity,
                newItem: MessageEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


}