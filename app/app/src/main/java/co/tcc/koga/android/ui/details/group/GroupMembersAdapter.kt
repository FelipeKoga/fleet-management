package co.tcc.koga.android.ui.details.group

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.data.database.entity.ChatEntity
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowUserBinding
import co.tcc.koga.android.utils.Constants
import co.tcc.koga.android.utils.show


class GroupMembersAdapter(private val chat: ChatEntity, private val currentUsername: String) :
    ListAdapter<UserEntity, GroupMembersAdapter.GroupMemberViewHolder>(DIFF_CALLBACK) {

    var onLoadAvatar: ((avatar: String?, imageView: ImageView) -> Unit)? = null
    var onMemberClicked: ((member: UserEntity) -> Unit)? = null

    class GroupMemberViewHolder(val binding: RowUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            chat: ChatEntity,
            member: UserEntity,
            currentUsername: String,
            onLoadAvatar: ((avatar: String?, imageView: ImageView) -> Unit)?,
            onMemberClicked: ((member: UserEntity) -> Unit)?
        ) {
            binding.run {
                textViewName.text = member.name
                textViewEmail.text = member.email
                onLoadAvatar?.invoke(
                    member.avatar ?: Constants.getAvatarURL(
                        member.name,
                        member.color
                    ), imageViewAvatar
                )

                if (chat.admin == currentUsername && member.username != currentUsername) {
                    imageViewRemove.show()
                }

                if (member.username == currentUsername) {
                    textViewAdmin.show()
                }

                linearLayoutUserContainer.setOnClickListener {
                    if (member.username != currentUsername) {
                        onMemberClicked?.invoke(member)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberViewHolder {
        return GroupMemberViewHolder(RowUserBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int) {
        holder.bind(chat, getItem(position), currentUsername, onLoadAvatar, onMemberClicked)
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
