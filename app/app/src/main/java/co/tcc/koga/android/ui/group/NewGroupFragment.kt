package co.tcc.koga.android.ui.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.drivefy.android.adapter.SelectContactAdapter
import co.tcc.koga.android.databinding.NewGroupFragmentBinding


class NewGroupFragment : Fragment() {

    private lateinit var binding: NewGroupFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = NewGroupFragmentBinding.inflate(inflater)
        binding.apply {
            recyclerViewContactsNewGroup.layoutManager = LinearLayoutManager(container?.context)
//            recyclerViewContactsNewGroup.adapter = SelectContactAdapter(
//                Database.getContactsOnly(),
//                requireContext()
//            ) { }

            toolbarNewGroup.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
        return binding.root
    }

}