package co.tcc.koga.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowUserBinding
import co.tcc.koga.android.utils.Constants

class UserAdapter : ListAdapter<UserEntity, UserAdapter.UserViewHolder>(DIFF_CALLBACK) {

    var onLoadAvatar: ((avatar: String?, imageView: ImageView) -> Unit)? = null
    var onUserClicked: ((user: UserEntity) -> Unit)? = null

    class UserViewHolder(val binding: RowUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            user: UserEntity,
            onLoadAvatar: ((avatar: String?, imageView: ImageView) -> Unit)?,
            onUserClicked: ((user: UserEntity) -> Unit)?
        ) {
            binding.run {
                textViewName.text = user.name
                textViewEmail.text = user.email
                onLoadAvatar?.invoke(
                    user.avatar ?: Constants.getAvatarURL(user.name, user.color),
                    imageViewAvatar
                )
                linearLayoutUserContainer.setOnClickListener {
                    onUserClicked?.invoke(user)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(RowUserBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position), onLoadAvatar, onUserClicked)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UserEntity>() {
            override fun areItemsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
                return oldItem.username == newItem.username
            }

            override fun areContentsTheSame(
                oldItem: UserEntity,
                newItem: UserEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}