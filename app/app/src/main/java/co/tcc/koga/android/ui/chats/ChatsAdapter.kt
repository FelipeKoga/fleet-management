package co.tcc.koga.android.ui.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.ChatEntity
import kotlinx.android.synthetic.main.row_chat.view.*

class ChatsAdapter(
    var chats: List<ChatEntity>,
    var onContactClicked: (chat: ChatEntity) -> Unit
) :
    RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    class ChatsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val view: View = itemView
        private val textViewName: TextView = itemView.text_view_name

        fun bindView(chat: ChatEntity, onContactClicked: (chat: ChatEntity) -> Unit) {
            textViewName.text = chat.user?.name
            view.setOnClickListener { onContactClicked.invoke(chat) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_chat, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return chats.size
    }

//    override fun getItemViewType(position: Int): Int =
//        if (contacts[position].isGroup) viewGroupItem else viewContactItem

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        val chat = chats[position]
        holder.bindView(chat, onContactClicked)
//        if (holder is DataViewHolder) {
//            holder.name.text = chat.user.name
////            Glide
////                .with(context)
////                .load(contact.photo)
////                .centerCrop()
////                .error(if (contact.isGroup) R.drawable.ic_round_group else R.drawable.ic_round_person)
////                .placeholder(if (contact.isGroup) R.drawable.ic_round_group else R.drawable.ic_round_person)
////                .apply(RequestOptions.circleCropTransform())
////                .into(holder.photo)
//            holder.view.setOnClickListener {
//                onContactClicked(chat)
//            }
//        }

    }

}
