package co.tcc.koga.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowAudioMessageReceivedBinding
import co.tcc.koga.android.databinding.RowAudioMessageSentBinding
import co.tcc.koga.android.databinding.RowMessageReceivedBinding
import co.tcc.koga.android.databinding.RowMessageSentBinding
import co.tcc.koga.android.utils.getHourByTimestamp
import co.tcc.koga.android.utils.show


class ChatAdapter(
    var messages: List<MessageEntity>,
    var username: String,
    var members: List<UserEntity>?
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
                if (!item.timestamp.isNullOrEmpty()) {
                    textViewMessageSentDate.text = getHourByTimestamp(item.timestamp)

                }
                if (item.status === "sent") {
                    imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_check)
                } else {
                    imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_timer)
                }
            }
        }
    }

    class SenderAudioViewHolder(private val binding: RowAudioMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageEntity) {
            with(binding) {
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
        fun bind(item: MessageEntity, members: List<UserEntity>?) {
            with(binding) {
                if (!members.isNullOrEmpty()) {
                    val user = members.find { item.username == it.username }
                    linearLayoutMessageHeader.show()
                    linearLayoutMessageBody.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    textViewMessageSender.text = user?.fullName

                }
                textViewMessageReceived.text = item.message
                if (!item.timestamp.isNullOrEmpty()) {
                    textViewMessageReceivedHour.text = getHourByTimestamp(item.timestamp)

                }
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
            return SenderAudioViewHolder(
                RowAudioMessageSentBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    )
                )
            )
        }
        if (viewType == viewSenderItem) {
            return SenderViewHolder(RowMessageSentBinding.inflate(LayoutInflater.from(parent.context)))

        }
        if (viewType == viewRecipientItem) {
            return RecipientViewHolder(RowMessageReceivedBinding.inflate(LayoutInflater.from(parent.context)))
        }

        if (viewType == viewRecipientAudioItem) {
            return RecipientAudioViewHolder(
                RowAudioMessageReceivedBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    )
                )
            )
        }

        return SenderViewHolder(RowMessageSentBinding.inflate(LayoutInflater.from(parent.context)))
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
            is SenderViewHolder -> holder.bind(message)
            is RecipientViewHolder -> holder.bind(message, members)
            is SenderAudioViewHolder -> holder.bind(message)
            is RecipientAudioViewHolder -> holder.bind(message)
        }
    }

}