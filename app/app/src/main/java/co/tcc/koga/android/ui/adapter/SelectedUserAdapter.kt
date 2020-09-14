package co.tcc.koga.android.ui.adapter

import kotlinx.android.synthetic.main.row_selected_user.view.*

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.domain.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SelectedUserAdapter(
    var context: Context,
    var users: List<User>,
    var onUserRemoved: (user: User) -> Unit
) :
    RecyclerView.Adapter<SelectedUserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val view = itemView
        private val textViewName: TextView = itemView.text_view_selected_user
        private val imageViewAvatar: ImageView = itemView.image_view_selected_user_avatar

        fun bindView(user: User, context: Context, onUserRemoved: (user: User) -> Unit) {
            textViewName.text = user.fullName
            view.setOnClickListener {
                user.isSelected = false
                onUserRemoved(user)
            }
            bindAvatar(user, context)
        }

        private fun bindAvatar(user: User, context: Context) {
            Glide
                .with(context)
                .load(user.avatar)
                .centerInside()
                .apply(RequestOptions.circleCropTransform())
                .error(R.drawable.ic_round_person)
                .placeholder(R.drawable.ic_round_person)
                .into(imageViewAvatar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_selected_user, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return users.size
    }


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val users = users[position]
        holder.bindView(users, context, onUserRemoved)
    }

}