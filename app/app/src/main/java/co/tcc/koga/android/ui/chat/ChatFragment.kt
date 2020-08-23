package co.tcc.koga.android.ui.chat

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.R
import co.tcc.koga.android.data.Database
import co.tcc.koga.android.databinding.ChatFragmentBinding
import co.tcc.koga.android.databinding.ContactDetailsFragmentBinding
import co.tcc.koga.android.domain.Contact
import co.tcc.koga.android.domain.ContactStatus
import co.tcc.koga.android.domain.Message
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.contact_details_fragment.view.*

class ChatFragment : Fragment() {

    private lateinit var binding: ChatFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChatFragmentBinding.inflate(inflater)
        binding.apply {
            binding.root.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity?.window?.statusBarColor = Color.WHITE

            loadContactPhoto(ivChatPhoto)
            tbChat.inflateMenu(R.menu.chat_menu)
            tbChat.setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)
                true
            }
            recyclerViewChatMessages.layoutManager = LinearLayoutManager(context)
            val chatAdapter = ChatAdapter(
                Database.getMessages()
            )
            recyclerViewChatMessages.adapter = chatAdapter
            chatAdapter.notifyDataSetChanged()
            tbChat.setOnClickListener {
                val contactDetailsBinding = ContactDetailsFragmentBinding.inflate(inflater)
                loadContactPhoto(contactDetailsBinding.imageViewContactDetailsPhoto)
                MaterialAlertDialogBuilder(context as Context)
                    .setView(contactDetailsBinding.root)
                    .show()
            }
            tbChat.setNavigationOnClickListener {
                activity?.window?.statusBarColor = ContextCompat
                    .getColor(activity as Activity, R.color.primaryColor)
                findNavController().popBackStack()
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_speaker -> {
                with(binding) {
                    linearLayoutChatSpeaker.visibility = View.VISIBLE
                    linearLayoutChatKeyboard.visibility = View.GONE
                    item.isVisible = false
                    tbChat.menu.findItem(R.id.nav_keyboard).isVisible = true
                }
                true
            }
            R.id.nav_keyboard -> {
                with(binding) {
                    linearLayoutChatSpeaker.visibility = View.GONE
                    linearLayoutChatKeyboard.visibility = View.VISIBLE
                    item.isVisible = false
                    tbChat.menu.findItem(R.id.nav_speaker).isVisible = true
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadContactPhoto(imageView: ImageView) {
        Glide
            .with(binding.root)
            .load("https://www.osprofanos.com/wp-content/uploads/2013/03/cat37.jpg")
            .centerCrop()
            .apply(RequestOptions.circleCropTransform())
            .error(R.drawable.ic_outline_person)
            .placeholder(R.drawable.ic_round_person)
            .into(imageView)
    }
}