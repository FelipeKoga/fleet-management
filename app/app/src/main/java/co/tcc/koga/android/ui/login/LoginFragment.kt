package co.tcc.koga.android.ui.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.MainActivity
import co.tcc.koga.android.R
import kotlinx.android.synthetic.main.login_fragment.*
import javax.inject.Inject


class LoginFragment : Fragment(R.layout.login_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<LoginViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_login.setOnClickListener {
            viewModel.authenticate(
                text_input_login.text.toString(),
                text_innput_password.text.toString()
            )
        }

        viewModel.loadingSignIn.observe(viewLifecycleOwner, {  isLoading ->
            button_login.visibility = if (isLoading) View.GONE else View.VISIBLE
            progress_bar_login.visibility = if (isLoading) View.VISIBLE else View.GONE

        })

        viewModel.signInStatus.observe(viewLifecycleOwner, { isSignIn ->
            findNavController().navigate(
                R.id.action_loginFragment_to_chatsFragment,
            )
        })
    }

}