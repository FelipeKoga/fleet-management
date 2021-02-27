package co.tcc.koga.android.ui.chat.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowMessageReceivedBinding
import co.tcc.koga.android.utils.FormatterUtil
import co.tcc.koga.android.utils.show

class RecipientViewHolder(private val binding: RowMessageReceivedBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: MessageEntity, members: List<UserEntity>?) {
        binding.run {
            if (!members.isNullOrEmpty()) {
                val user = members.find { item.username == it.username }
                textViewUserName.show()
                textViewUserName.text = user?.customName ?: user?.name
            }

            textViewMessageReceived.text = item.message
            textViewMessageReceivedHour.text = FormatterUtil.getHour(item.createdAt)
        }
    }


    companion object {
        fun create(
            parent: ViewGroup,
        ): RecipientViewHolder {
            val binding = RowMessageReceivedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return RecipientViewHolder(binding)
        }
    }
}