package co.tcc.koga.android.ui.contacts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.domain.Contact
import co.tcc.koga.android.ui.contacts.ContactsFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.row_contact.view.*

class ContactsAdapter(
    private var contacts: List<Contact>,
    var context: Context,
    var onContactClicked: (contact: Contact) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val viewContactItem = 2
    private val viewGroupItem = 1

    class DataViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val name = itemView.tv_name as TextView

        val photo = itemView.iv_photo as ImageView
        val view = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == viewContactItem) {
            DataViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_contact, parent, false)
            )
        } else {
            DataViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_group, parent, false)
            )
        }

    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun getItemViewType(position: Int): Int =
        if (contacts[position].isGroup) viewGroupItem else viewContactItem

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val contact = contacts[position]
        if (holder is DataViewHolder) {
            holder.name.text = contact.name
            Glide
                .with(context)
                .load(contact.photo)
                .centerCrop()
                .error(if (contact.isGroup) R.drawable.ic_round_group else R.drawable.ic_round_person)
                .placeholder(if (contact.isGroup) R.drawable.ic_round_group else R.drawable.ic_round_person)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.photo)
            holder.view.setOnClickListener {
                onContactClicked(contact)
            }
        }

    }

}
