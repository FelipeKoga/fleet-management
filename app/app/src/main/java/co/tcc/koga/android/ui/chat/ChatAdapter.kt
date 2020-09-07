package co.tcc.koga.android.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.network.Client
import co.tcc.koga.android.databinding.RowAudioMessageReceivedBinding
import co.tcc.koga.android.databinding.RowAudioMessageSentBinding
import co.tcc.koga.android.databinding.RowMessageReceivedBinding
import co.tcc.koga.android.databinding.RowMessageSentBinding
import co.tcc.koga.android.utils.getDateByTimestamp
import co.tcc.koga.android.utils.getHourByTimestamp


class ChatAdapter(
    var messages: List<MessageEntity>,
    var userId: String = ""
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewSenderItem = 2
    private val viewSenderAudioItem = 1
    private val viewRecipientItem = 3
    private val viewRecipientAudioItem = 4

    class SenderViewHolder(private val binding: RowMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageEntity) {
            with(binding) {
                textViewMessageSent.text = item.message
                textViewMessageSentDate.text = getHourByTimestamp(item.createdAt)
                if (item.id.isNullOrEmpty()) {
                    imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_timer);
                } else {
                    imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_check);
                }
            }
        }
    }

    class SenderAudioViewHolder(private val binding: RowAudioMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageEntity) {
            with(binding) {
                println("BINDING")
//                Glide
//                    .with(binding.root)
//                    .load(item.sender.photo)
//                    .centerCrop()
//                    .apply(RequestOptions.circleCropTransform())
//                    .error(R.drawable.ic_round_person)
//                    .placeholder(R.drawable.ic_round_person)
//                    .into(imageViewUserPhoto)
            }
        }
    }

    class RecipientViewHolder(private val binding: RowMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageEntity) {
            with(binding) {
                textViewMessageRecipient.text = item.message
                textViewMessageRecipientHour.text = getHourByTimestamp(item.createdAt)
            }
        }
    }

    class RecipientAudioViewHolder(private val binding: RowAudioMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageEntity) {
            with(binding) {
//                Glide
//                    .with(binding.root)
//                    .load(item.recipient.photo)
//                    .centerCrop()
//                    .apply(RequestOptions.circleCropTransform())
//                    .error(R.drawable.ic_round_person)
//                    .placeholder(R.drawable.ic_round_person)
//                    .into(imageViewUserPhoto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == viewSenderAudioItem) {
            return SenderAudioViewHolder(RowAudioMessageSentBinding.inflate(LayoutInflater.from(parent.context)))
        }
        if (viewType == viewSenderItem) {
            return SenderViewHolder(RowMessageSentBinding.inflate(LayoutInflater.from(parent.context)))

        }
        if (viewType == viewRecipientItem){
            return RecipientViewHolder(RowMessageReceivedBinding.inflate(LayoutInflater.from(parent.context)))
        }

        if (viewType == viewRecipientAudioItem) {
            return  RecipientAudioViewHolder(RowAudioMessageReceivedBinding.inflate(LayoutInflater.from(parent.context)))
        }

        return SenderViewHolder(RowMessageSentBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        if (message.senderId == userId) {
            return viewSenderItem
        }
        return viewRecipientItem
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when(holder) {
            is SenderViewHolder -> holder.bind(message)
            is RecipientViewHolder -> holder.bind(message)
            is SenderAudioViewHolder -> holder.bind(message)
            is RecipientAudioViewHolder -> holder.bind(message)
        }
    }

}