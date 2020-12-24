package co.tcc.koga.android.ui.auth.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.LoginFragmentBinding
import co.tcc.koga.android.utils.AUTH_STATUS
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = LoginFragmentBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edit_text_password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.authenticate()
                true
            } else {
                false
            }
        }


        viewModel.formErrors.observe(viewLifecycleOwner, { errors ->
            clearError()
            for ((key, value) in errors) {
                if (key === "username") {
                    text_input_username.error = value
                }

                if (key === "password") {
                    text_input_password.error = value
                }
            }
        })


        viewModel.authenticationStatus.observe(viewLifecycleOwner, { authenticationStatus ->
            text_view_login_error.visibility = View.GONE
            when (authenticationStatus) {
                AUTH_STATUS.LOGGED_IN -> {
                    findNavController().navigate(
                        R.id.action_loginFragment_to_chatsFragment,
                    )
                }

                AUTH_STATUS.UNAUTHORIZED -> {
                    text_view_login_error.visibility = View.VISIBLE
                    text_view_login_error.text = getString(R.string.unauthorized)
                }

                AUTH_STATUS.ERROR -> {
                    text_view_login_error.visibility = View.VISIBLE
                    text_view_login_error.text = getString(R.string.loginError)
                }

                else -> getString(R.string.loginError)
            }

        })
    }

    private fun clearError() {
        text_view_login_error.visibility = View.GONE
        text_input_username.error = null
        text_input_password.error = null
    }

    private fun showErrorToast() {
        Toast.makeText(requireContext(), "Não foi possível realizar o login", Toast.LENGTH_LONG)
            .show()
    }


}