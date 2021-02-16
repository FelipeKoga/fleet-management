package co.tcc.koga.android.ui.chat.viewholder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.data.database.entity.MessageEntity
import co.tcc.koga.android.databinding.RowAudioMessageReceivedBinding
import co.tcc.koga.android.databinding.RowMessageReceivedBinding

class AudioRecipientViewHolder(private val binding: RowAudioMessageReceivedBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        item: MessageEntity,
        onPlayAudio: (url: String, context: Context) -> Unit,
        onStop: () -> Unit,
        onPause: () -> Unit,
        onSeek: () -> Unit
    ) {


        binding.apply {
            imageButtonPlayAudio.setOnClickListener {
                onPlayAudio(item.message, binding.root.context)
            }
        }
    }
}