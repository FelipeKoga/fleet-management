package co.tcc.koga.android.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.domain.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.row_select_user.view.*

class SelectUserAdapter(
    var context: Context,
    var users: List<User>,
    var onUserSelect: (user: User) -> Unit
) :
    RecyclerView.Adapter<SelectUserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val textViewName: TextView = itemView.text_view_select_user_name
        private val textViewPhone: TextView = itemView.text_view_select_user_phone
        private val imageViewAvatar: ImageView = itemView.image_view_select_user_avatar
        private val checkBox: CheckBox = itemView.check_box_select_user

        fun bindView(user: User, context: Context, onUserSelect: (user: User) -> Unit) {
            textViewName.text = user.fullName
            textViewPhone.text = user.email
            checkBox.isChecked = user.isSelected
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                user.isSelected = isChecked
                onUserSelect(user)
            }
            bindAvatar(user, context)
        }

        private fun bindAvatar(user: User, context: Context) {
            println(user)
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
                .inflate(R.layout.row_select_user, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return users.size
    }


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val users = users[position]
        holder.bindView(users, context, onUserSelect)

    }

}