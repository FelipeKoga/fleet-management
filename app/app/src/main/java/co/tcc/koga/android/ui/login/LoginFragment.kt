package co.tcc.koga.android.ui.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.data.Resource
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

        viewModel.loadingSignIn.observe(viewLifecycleOwner, { isLoading ->
            button_login.visibility = if (isLoading) View.GONE else View.VISIBLE
            progress_bar_login.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.signInStatus.observe(viewLifecycleOwner, { isSignIn ->
            viewModel.initCurrentUser().observe(viewLifecycleOwner, {
                if (it.status == Resource.Status.SUCCESS) {
                    findNavController().navigate(
                        R.id.action_loginFragment_to_chatsFragment,
                    )
                } else if (it.status == Resource.Status.ERROR) {
                    showErrorToast()
                }

            })
        })
    }

    private fun showErrorToast() {
        Toast.makeText(requireContext(), "Não foi possível realizar o login", Toast.LENGTH_LONG)
            .show()
    }

}