package co.tcc.koga.android.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.data.network.aws.Client
import co.tcc.koga.android.databinding.RowMessageReceivedBinding
import co.tcc.koga.android.databinding.RowMessageSentBinding
import co.tcc.koga.android.utils.getHour
import co.tcc.koga.android.utils.show


class ChatAdapter(
    var username: String,
    var members: List<UserEntity>? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val viewSenderItem = 1
    private val viewRecipientItem = 3
    private val messages: MutableList<MessageEntity> = mutableListOf()

    class SenderViewHolder(private val binding: RowMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageEntity, members: List<UserEntity>?) {
            binding.apply {
                textViewMessageSent.text = if (item.hasAudio) "Áudio" else item.message
                if (!item.createdAt.isNullOrEmpty()) {
                    textViewMessageSentDate.text = getHour(item.createdAt)

                }
                if (item.status == "SENT") {
                    imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_check)
                } else {
                    imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_timer)
                }
            }
        }
    }

    class RecipientViewHolder(private val binding: RowMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageEntity, members: List<UserEntity>?) {
            binding.apply {
                if (!members.isNullOrEmpty()) {
                    val user = members.find { item.username == it.username }
                    textViewMessageSender.show()
                    textViewMessageSender.text = user?.name

                }
                textViewMessageReceived.text = if (item.hasAudio) "Áudio" else item.message
                if (!item.createdAt.isNullOrEmpty()) {
                    textViewMessageReceivedHour.text = getHour(item.createdAt)

                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == viewSenderItem) {
            return SenderViewHolder(RowMessageSentBinding.inflate(LayoutInflater.from(parent.context)))

        }
        if (viewType == viewRecipientItem) {
            return RecipientViewHolder(RowMessageReceivedBinding.inflate(LayoutInflater.from(parent.context)))
        }

        return SenderViewHolder(RowMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        if (message.username == username) {
            return viewSenderItem
        }
        return viewRecipientItem
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SenderViewHolder -> holder.bind(message, members)
            is RecipientViewHolder -> holder.bind(message, members)
        }
    }

    fun load(items: List<MessageEntity>) {
        messages.clear()
        messages.addAll(items)
        notifyDataSetChanged()
    }

    fun add(message: MessageEntity) {
        println(message)
        val found = messages.find { it.messageId === message.messageId }
        println(found)
        if (found !== null) {
            messages.addAll(messages.map { if (it.messageId === message.messageId) message else it })
        } else {
            messages.add(message)
        }
        println(messages)
        notifyDataSetChanged()
    }

}