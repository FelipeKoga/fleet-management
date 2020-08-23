package co.tcc.koga.android.ui.new_chat

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.tcc.koga.android.R
import co.tcc.koga.android.data.Database
import co.tcc.koga.android.databinding.NewChatFragmentBinding
import co.tcc.koga.android.ui.contacts.ContactsAdapter

class NewChatFragment : Fragment() {
    private lateinit var binding: NewChatFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = NewChatFragmentBinding.inflate(inflater)
        binding.apply {
            recyclerViewSearchContacts.layoutManager = LinearLayoutManager(container?.context)
            recyclerViewSearchContacts.adapter = ContactsAdapter(
                Database.getContactsOnly(),
                requireContext()
            ) { contact ->
            }

            toolbarSearchContacts.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
//
//            floatingButtonNewGroup.setOnClickListener {
//                findNavController().navigate(R.id.action_newChatFragment_to_newGroupFragment)
//            }
        }
        return binding.root
    }


}