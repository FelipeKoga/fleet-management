package co.tcc.koga.android.ui.contacts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.SearchContactsFragmentBinding
import co.tcc.koga.android.domain.Contact

class SearchContactsFragment : Fragment() {
    private lateinit var binding: SearchContactsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SearchContactsFragmentBinding.inflate(inflater)
        binding.apply {
//            recyclerViewSearchContacts.layoutManager = LinearLayoutManager(container?.context)
//            recyclerViewSearchContacts.adapter = ContactsAdapter(
//                Database.getContactsOnly(),
//                requireContext()
//            ) { contact ->
//                redirectToChat(contact)
//            }
//
//            toolbarSearchContacts.setNavigationOnClickListener {
//                findNavController().popBackStack()
//            }
        }
        return binding.root
    }

    private fun redirectToChat(contact: Contact) {
        findNavController().navigate(
            R.id.action_searchContactsFragment_to_chatFragment,
            bundleOf("contact" to contact)
        )

    }
}