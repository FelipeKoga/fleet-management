package co.tcc.koga.android.ui.chat.viewholder

import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowMessageReceivedBinding
import co.tcc.koga.android.utils.getHour
import co.tcc.koga.android.utils.show

class RecipientViewHolder(private val binding: RowMessageReceivedBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: MessageEntity, members: List<UserEntity>?) {
        binding.apply {
            if (!members.isNullOrEmpty()) {
                val user = members.find { item.username == it.username }
                textViewMessageSender.show()
                textViewMessageSender.text = user?.name

            }
            textViewMessageReceived.text = item.message
            textViewMessageReceivedHour.text = getHour(item.createdAt)

        }
    }
}