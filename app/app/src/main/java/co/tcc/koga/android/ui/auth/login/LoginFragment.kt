package co.tcc.koga.android.ui.auth.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.LoginFragmentBinding
import co.tcc.koga.android.utils.AUTH_STATUS
import javax.inject.Inject


class LoginFragment : Fragment(R.layout.login_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<LoginViewModel> { viewModelFactory }
    private lateinit var binding: LoginFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editTextPassword.setOnEditorActionListener { _, actionId, _ ->
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
                    binding.textInputUsername.error = value
                }

                if (key === "password") {
                    binding.textInputPassword.error = value
                }
            }
        })

        binding.buttonForgotPassword.setOnClickListener { goToForgotPassword() }


        viewModel.authenticationStatus.observe(viewLifecycleOwner, { authenticationStatus ->
            binding.textViewLoginError.visibility = View.GONE
            when (authenticationStatus) {
                AUTH_STATUS.LOGGED_IN -> {
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToChatsFragment(),
                    )
                }

                AUTH_STATUS.UNAUTHORIZED -> {
                   binding.textViewLoginError.apply {
                       visibility = View.VISIBLE
                       text = getString(R.string.unauthorized)
                   }
                }

                AUTH_STATUS.ERROR -> {
                    binding.textViewLoginError.apply {
                        visibility = View.VISIBLE
                        text = getString(R.string.loginError)
                    }
                }

                else -> getString(R.string.loginError)
            }

        })
    }

    private fun goToForgotPassword() {
        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }


    private fun clearError() {
        binding.apply {
            textViewLoginError.visibility = View.GONE
            textInputUsername.error = null
            textInputPassword.error = null
        }

    }
}