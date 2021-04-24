package co.tcc.koga.android.ui.adapter

import kotlinx.android.synthetic.main.row_selected_user.view.*

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.data.database.entity.UserEntity
import co.tcc.koga.android.databinding.RowSelectedUserBinding
import co.tcc.koga.android.databinding.RowUserBinding
import co.tcc.koga.android.utils.Constants

class SelectedUserAdapter :
    ListAdapter<UserEntity, SelectedUserAdapter.SelectedUserViewHolder>(DIFF_CALLBACK) {


    var onLoadAvatar: ((avatar: String?, imageView: ImageView) -> Unit)? = null
    var onUserClicked: ((user: UserEntity) -> Unit)? = null

    class SelectedUserViewHolder(val binding: RowSelectedUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(
            user: UserEntity,
            onUserClicked: ((user: UserEntity) -> Unit)?,
            onLoadAvatar: ((avatar: String?, imageView: ImageView) -> Unit)?
        ) {
            binding.run {
                textViewSelectedUser.text = user.name
                imageViewRemoveSelected.setOnClickListener { onUserClicked?.invoke(user) }
                onLoadAvatar?.invoke(
                    if (user.avatar.isNullOrEmpty()) Constants.getAvatarURL(
                        user.name,
                        user.color
                    ) else user.avatar,
                    imageViewSelectedUserAvatar
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedUserViewHolder {
        return SelectedUserViewHolder(RowSelectedUserBinding.inflate(LayoutInflater.from(parent.context)))
    }


    override fun onBindViewHolder(holder: SelectedUserViewHolder, position: Int) {
        holder.bindView(getItem(position), onUserClicked, onLoadAvatar)
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