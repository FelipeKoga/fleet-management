package co.drivefy.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import co.tcc.koga.android.R
import co.tcc.koga.android.domain.Contact
import kotlinx.android.synthetic.main.row_contact.view.*

class SelectContactAdapter(
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
        return DataViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_select_contact, parent, false)
        )


    }

    override fun getItemCount(): Int {
        return contacts.size
    }

//    override fun getItemViewType(position: Int): Int =
//        if (contacts[position].isGroup) viewGroupItem else viewContactItem

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val contact = contacts[position]
        if (holder is DataViewHolder) {
            holder.name.text = contact.name
//            Glide
//                .with(context)
////                .load("https://scontent.fpgz1-1.fna.fbcdn.net/v/t31.0-8/23509456_1409309355854887_1466911775224434071_o.jpg?_nc_cat=108&_nc_sid=09cbfe&_nc_eui2=AeG623DjsuyKytHojdUetGk7pVh_hLQD8-ulWH-EtAPz68zvBVOrDuTeDTDkcwcMKguceUm-oHC3hNPbdhEQuVZK&_nc_ohc=inhZTXaPe04AX-lM6La&_nc_ht=scontent.fpgz1-1.fna&oh=3c51f2abbda394c3c8165b3abe21e0c3&oe=5F2B1497")
//                .load(contact.photo
//                )
//                .centerCrop()
//                .error(if (contact.isGroup) R.drawable.ic_round_group else R.drawable.ic_round_person)
//                .placeholder(if (contact.isGroup) R.drawable.ic_round_group else R.drawable.ic_round_person)
//                .apply(RequestOptions.circleCropTransform())
//                .into(holder.photo)
            holder.view.setOnClickListener {
                onContactClicked(contact)
            }
        }

    }

}
