package co.tcc.koga.android.ui.contacts

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.R
import co.tcc.koga.android.data.Database
import co.tcc.koga.android.databinding.ContactsFragmentBinding
import co.tcc.koga.android.domain.Contact
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class ContactsFragment : Fragment() {
    private lateinit var binding: ContactsFragmentBinding
    private val viewModel: ContactsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ContactsFragmentBinding.inflate(inflater)
        binding.apply {
            toolbarProfile.inflateMenu(R.menu.contacts_menu)
            toolbarProfile.setOnMenuItemClickListener { item ->
                onOptionsItemSelected(item)
                true
            }
            toolbarProfileContent.setOnClickListener {
                findNavController().navigate(
                    R.id.action_contactsFragment_to_profileFragment
                )

            }

            recyclerViewContacts.layoutManager = LinearLayoutManager(container?.context)
            recyclerViewContacts.adapter = ContactsAdapter(
                Database.getContacts(),
                requireContext()
            ) { contact ->
                redirectToChat(contact)
            }

            viewModel.getContacts()
//            loadContactPhoto()
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.contacts_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_search -> {
                findNavController().navigate(
                    R.id.action_contactsFragment_to_searchContactsFragment
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun redirectToNewGroup(view: View) {
        findNavController().navigate(R.id.action_contactsFragment_to_newGroupFragment)
    }

    private fun redirectToChat(contact: Contact) {
        findNavController().navigate(
            R.id.action_nav_contacts_to_chatFragment,
            bundleOf("contact" to contact)
        )

    }

    private fun loadContactPhoto() {
        Glide
            .with(this)
            .load("https://scontent.fpgz1-1.fna.fbcdn.net/v/t31.0-8/23509456_1409309355854887_1466911775224434071_o.jpg?_nc_cat=108&_nc_sid=09cbfe&_nc_eui2=AeG623DjsuyKytHojdUetGk7pVh_hLQD8-ulWH-EtAPz68zvBVOrDuTeDTDkcwcMKguceUm-oHC3hNPbdhEQuVZK&_nc_ohc=inhZTXaPe04AX-lM6La&_nc_ht=scontent.fpgz1-1.fna&oh=3c51f2abbda394c3c8165b3abe21e0c3&oe=5F2B1497")
            .centerCrop()
            .apply(RequestOptions.circleCropTransform())
            .error(R.drawable.ic_outline_person)
            .placeholder(R.drawable.ic_round_person)
            .into(binding.ivChatPhoto)
    }

}

