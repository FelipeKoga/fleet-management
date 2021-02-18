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
    var onContactClicked: (chat: ChatEntity) -> Unit
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
            return getHour(message.createdAt)
        }

        fun bind(
            chat: ChatEntity,
            onLoadAvatar: (avatar: String?, isGroup: Boolean, imageView: ImageView) -> Unit,
            onContactClicked: (chat: ChatEntity) -> Unit
        ) {


            binding.apply {
                if (chat.messages.isNotEmpty()) {
                    textViewLastMessage.text = getLastMessage(chat.messages.last())
                    textViewLastMessageHour.text = getMessageHour(chat.messages.last())
                }

                if (chat.user !== null) {
                    if (chat.user?.status == "ONLINE") {
                        imageViewUserStatusOnline.show()
                        imageViewUserStatusOffline.hide()
                    } else {
                        imageViewUserStatusOnline.hide()
                        imageViewUserStatusOffline.show()
                    }
                    onLoadAvatar(
                        if (chat.user?.avatarUrl != "") chat.user?.avatarUrl else getUserAvatar(
                            chat.user as UserEntity
                        ),
                        false,
                        imageViewAvatar
                    )
                    textViewName.text = chat.user?.name
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

                linearLayoutRowChat.setOnClickListener { onContactClicked.invoke(chat) }
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
        holder.bind(chats[position], onLoadAvatar, onContactClicked)
    }

    fun load(items: List<ChatEntity>) {
        chats.clear()
        chats.addAll(items)
        notifyDataSetChanged()
    }

}
