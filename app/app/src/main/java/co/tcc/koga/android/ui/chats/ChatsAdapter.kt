package co.tcc.koga.android.ui.chats

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowChatBinding
import co.tcc.koga.android.utils.*

class ChatsAdapter : ListAdapter<ChatEntity, ChatsAdapter.ChatsViewHolder>(DIFF_CALLBACK) {

    var onLoadAvatar: ((avatar: String?, isGroup: Boolean, imageView: ImageView) -> Unit)? = null
    var onChatClicked: ((chat: ChatEntity) -> Unit)? = null

    class ChatsViewHolder(val binding: RowChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private fun getLastMessage(message: MessageEntity?): String {
            if (message === null) return ""
            if (message.hasAudio) return "Áudio"
            return message.message
        }

        private fun getMessageHour(message: MessageEntity?): String {
            if (message === null) return ""
            return FormatterUtil.getHour(message.createdAt)
        }

        fun bind(
            chat: ChatEntity,
            onLoadAvatar: ((avatar: String?, isGroup: Boolean, imageView: ImageView) -> Unit)?,
            onChatClicked: ((chat: ChatEntity) -> Unit)?
        ) {
            binding.apply {
                if (chat.messages.isNotEmpty()) {
                    textViewLastMessage.text = getLastMessage(chat.messages.last())
                    textViewLastMessageHour.text = getMessageHour(chat.messages.last())
                }

                imageViewUserStatusOffline.hide()
                imageViewUserStatusOnline.hide()

                if (chat.user !== null) {
                    val user = chat.user as UserEntity
                    if (user.status == "ONLINE") {
                        imageViewUserStatusOnline.show()
                        imageViewUserStatusOffline.hide()
                    } else {
                        imageViewUserStatusOnline.hide()
                        imageViewUserStatusOffline.show()
                    }

                    imageViewMessageStatus.hide()
                    if (chat.messages.isNotEmpty()) {
                        imageViewMessageStatus.show()
                        if (chat.messages.last()?.status == "SENT") {
                            imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_check)
                        } else {
                            imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_timer)
                        }
                    }

                    onLoadAvatar?.invoke(
                        if (user.avatar.isNullOrEmpty()) Constants.getAvatarURL(
                            user.name,
                            user.color
                        ) else user.avatar as String,
                        false,
                        imageViewAvatar
                    )
                    textViewName.text = user.name
                } else {
                    onLoadAvatar?.invoke(chat.avatar, true, imageViewAvatar)
                    textViewName.text = chat.groupName
                    imageViewUserStatusOffline.hide()
                    imageViewMessageStatus.hide()
                }

                if (chat.newMessages > 0) {
                    textViewNewMessages.show()
                    textViewNewMessages.text = chat.newMessages.toString()
                } else {
                    textViewNewMessages.hide()
                }

                linearLayoutRowChat.setOnClickListener { onChatClicked?.invoke(chat) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(RowChatBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bind(getItem(position), onLoadAvatar, onChatClicked)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatEntity>() {
            override fun areItemsTheSame(oldItem: ChatEntity, newItem: ChatEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ChatEntity,
                newItem: ChatEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}
