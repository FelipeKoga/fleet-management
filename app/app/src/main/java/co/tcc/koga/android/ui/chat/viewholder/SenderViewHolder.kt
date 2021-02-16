package co.tcc.koga.android.ui.chat.viewholder

import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowMessageSentBinding
import co.tcc.koga.android.utils.getHour

class SenderViewHolder(private val binding: RowMessageSentBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: MessageEntity, members: List<UserEntity>?) {
        binding.apply {
            textViewMessageSent.text = if (item.hasAudio) "Áudio" else item.message
            textViewMessageSentDate.text = getHour(item.createdAt)

            if (item.status == "SENT") {
                imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_check)
            } else {
                imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_timer)
            }
        }
    }
}