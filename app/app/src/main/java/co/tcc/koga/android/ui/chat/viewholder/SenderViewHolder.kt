package co.tcc.koga.android.ui.chat.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.databinding.RowMessageSentBinding
import co.tcc.koga.android.utils.FormatterUtil

class SenderViewHolder(private val binding: RowMessageSentBinding, ) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: MessageEntity) {
        binding.run {
            textViewMessageSent.text = item.message
            textViewMessageSentDate.text = FormatterUtil.getHour(item.createdAt)

            if (item.status == "SENT") {
                imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_check)
            } else {
                imageViewMessageStatus.setImageResource(R.drawable.ic_baseline_timer)
            }
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
        ): SenderViewHolder {
            val binding = RowMessageSentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            return SenderViewHolder(binding)
        }
    }
}