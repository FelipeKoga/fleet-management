package co.tcc.koga.android.ui.chats

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowChatBinding
import co.tcc.koga.android.utils.*

class ChatsAdapter(
    var onLoadAvatar: (avatar: String?, isGroup: Boolean, imageView: ImageView) -> Unit,
    var onChatClicked: (chat: ChatEntity) -> Unit
) :
    RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    private var chats: MutableList<ChatEntity> = mutableListOf()

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
            onLoadAvatar: (avatar: String?, isGroup: Boolean, imageView: ImageView) -> Unit,
            onChatClicked: (chat: ChatEntity) -> Unit
        ) {


            binding.apply {
                if (chat.messages.isNotEmpty()) {
                    textViewLastMessage.text = getLastMessage(chat.messages.last())
                    textViewLastMessageHour.text = getMessageHour(chat.messages.last())
                }

                if (chat.user !== null) {
                    val user = chat.user as UserEntity
                    if (user.status == "ONLINE") {
                        imageViewUserStatusOnline.show()
                        imageViewUserStatusOffline.hide()
                    } else {
                        imageViewUserStatusOnline.hide()
                        imageViewUserStatusOffline.show()
                    }
                    onLoadAvatar(
                        user.avatarUrl ?: Constants.getAvatarURL(user.name, user.color, 42),
                        false,
                        imageViewAvatar
                    )
                    textViewName.text = user.name
                } else {
                    onLoadAvatar(chat.avatar, true, imageViewAvatar)
                    textViewName.text = chat.groupName
                    imageViewUserStatusOffline.hide()
                }

                if (chat.newMessages > 0) {
                    textViewNewMessages.show()
                    textViewNewMessages.text = chat.newMessages.toString()
                } else {
                    textViewNewMessages.hide()
                }

                linearLayoutRowChat.setOnClickListener { onChatClicked.invoke(chat) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(RowChatBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bind(chats[position], onLoadAvatar, onChatClicked)
    }

    fun load(items: List<ChatEntity>) {
        chats.clear()
        chats.addAll(items)
        notifyDataSetChanged()
    }

}
