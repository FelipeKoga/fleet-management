package co.tcc.koga.android.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.ChatEntity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.row_chat.view.*

class ChatsAdapter(
    var context: Context,
    var chats: List<ChatEntity>,
    private var onContactClicked: (chat: ChatEntity) -> Unit
) :
    RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    class ChatsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val view: View = itemView
        private val textViewName: TextView = itemView.text_view_name
        private val imageViewAvatar: ImageView = itemView.image_view_avatar
        private val imageViewUserStatus: ImageView = itemView.image_view_user_status

        fun bindView(chat: ChatEntity, context: Context, onContactClicked: (chat: ChatEntity) -> Unit) {
            if (chat.isPrivate) {
                textViewName.text = chat.user?.fullName
            } else {
                imageViewUserStatus.visibility = View.GONE
                textViewName.text = chat.groupName
            }
            bindAvatar(chat, context)
            view.setOnClickListener { onContactClicked.invoke(chat) }
        }

        private fun bindAvatar(chat: ChatEntity, context: Context) {
            Glide
                .with(context)
                .load(
                    if (chat.isPrivate) chat.user?.avatar
                    else chat.avatar
                )
                .centerInside()
                .apply(RequestOptions.circleCropTransform())
                .error(if (chat.isPrivate) R.drawable.ic_round_person else R.drawable.ic_round_group)
                .placeholder(if (chat.isPrivate) R.drawable.ic_round_person else R.drawable.ic_round_group)
                .into(imageViewAvatar)
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

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        val chat = chats[position]
        holder.bindView(chat, context, onContactClicked)
    }

}
