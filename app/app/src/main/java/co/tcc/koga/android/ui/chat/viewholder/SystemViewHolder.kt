package co.tcc.koga.android.ui.chat.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.databinding.RowMessageSentBinding
import co.tcc.koga.android.databinding.RowSystemMessageBinding

class SystemViewHolder(private val binding: RowSystemMessageBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: MessageEntity) {
        binding.run {
            textViewSystemMessage.text = item.message
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
        ): SystemViewHolder {
            val binding = RowSystemMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SystemViewHolder(binding)
        }
    }
}