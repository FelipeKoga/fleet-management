package co.tcc.koga.android.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.utils.Avatar
import co.tcc.koga.android.utils.CONSTANTS
import co.tcc.koga.android.utils.getUserAvatar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.row_user.view.*

class UserAdapter(
    var context: Context,
    var users: List<UserEntity>,
    var onUserClicked: (user: UserEntity) -> Unit
) :
    RecyclerView.Adapter<UserAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val view: View = itemView
        private val textViewName: TextView = itemView.text_view_user_fullName
        private val textViewEmail: TextView = itemView.text_view_user_email
        private val imageViewAvatar: ImageView = itemView.image_view_user_avatar

        fun bindView(
            user: UserEntity,
            context: Context,
            onUserClicked: (user: UserEntity) -> Unit
        ) {
            textViewName.text = user.name
            textViewEmail.text = user.email
            view.setOnClickListener { onUserClicked(user) }
            bindAvatar(user, context)
        }

        private fun bindAvatar(user: UserEntity, context: Context) {
            Avatar.loadImage(context, imageViewAvatar, user.avatarUrl ?: getUserAvatar(user), R.drawable.ic_round_person)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        return MemberViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_user, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return users.size
    }


    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val users = users[position]
        holder.bindView(users, context, onUserClicked)

    }

}