package co.tcc.koga.android.ui.chat

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowAudioMessageReceivedBinding
import co.tcc.koga.android.databinding.RowAudioMessageSentBinding
import co.tcc.koga.android.databinding.RowMessageReceivedBinding
import co.tcc.koga.android.databinding.RowMessageSentBinding
import co.tcc.koga.android.ui.chat.viewholder.AudioRecipientViewHolder
import co.tcc.koga.android.ui.chat.viewholder.AudioSenderViewHolder
import co.tcc.koga.android.ui.chat.viewholder.RecipientViewHolder
import co.tcc.koga.android.ui.chat.viewholder.SenderViewHolder


class ChatAdapter(
    var username: String,
    var members: List<UserEntity>? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val viewSenderItem = 1
    private val viewRecipientItem = 2
    private val viewSenderAudioItem = 3
    private val viewRecipientAudioItem = 4
    private val messages: MutableList<MessageEntity?> = mutableListOf()
    private var mediaPlayer = MediaPlayer()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        if (viewType == viewSenderAudioItem) {
            return AudioSenderViewHolder(RowAudioMessageSentBinding.inflate(layoutInflater))
        }

        if (viewType == viewRecipientItem) {
            return RecipientViewHolder(RowMessageReceivedBinding.inflate(LayoutInflater.from(parent.context)))
        }

        if (viewType == viewRecipientAudioItem) {
            return AudioRecipientViewHolder(RowAudioMessageReceivedBinding.inflate(layoutInflater))
        }

        return SenderViewHolder(RowMessageSentBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        if (message?.username == username) {
            if (message.hasAudio) {
                return viewSenderAudioItem
            }
            return viewSenderItem
        }

        if (message!!.hasAudio) {
            return viewRecipientAudioItem
        }
        return viewRecipientItem
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SenderViewHolder -> holder.bind(message as MessageEntity, members)
            is RecipientViewHolder -> holder.bind(message as MessageEntity, members)
            is AudioSenderViewHolder -> holder.bind(message as MessageEntity)
            is AudioRecipientViewHolder -> holder.bind(
                message as MessageEntity,
                fun(url, context) { playAudio(url, context) },
                fun() {},
                fun() {},
                fun() {})
        }
    }

    private fun playAudio(url: String, context: Context) {
        println(url)
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }

    }

    fun pause() {}

    fun stopAudio() {}


    fun load(items: MutableList<MessageEntity?>) {
        messages.clear()
        messages.addAll(items)
        notifyDataSetChanged()
    }

    fun add(message: MessageEntity) {
        val found = messages.find { it?.messageId === message.messageId }
        if (found !== null) {
            messages.addAll(messages.map { if (it?.messageId === message.messageId) message else it })
        } else {
            messages.add(message)
        }
        notifyDataSetChanged()
    }

}