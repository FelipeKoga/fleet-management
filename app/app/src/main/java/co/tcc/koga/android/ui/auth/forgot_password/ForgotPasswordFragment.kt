package co.tcc.koga.android.ui.auth.forgot_password

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.ForgotPasswordFragmentBinding
import co.tcc.koga.android.ui.auth.ForgotPasswordStatus
import javax.inject.Inject


class ForgotPasswordFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ForgotPasswordViewModel> { viewModelFactory }
    private lateinit var binding: ForgotPasswordFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ForgotPasswordFragmentBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.formErrors.observe(viewLifecycleOwner, { errors ->
            clearError()
            for ((key, value) in errors) {
                if (key === "username") {
                    binding.textInputUsername.error = value
                }
            }
        })

        viewModel.passwordRecoverStatus.observe(viewLifecycleOwner) { status ->
            if (status === ForgotPasswordStatus.CODE_SENDED) {
                findNavController().navigate(
                    R.id.action_forgotPasswordFragment_to_confirmForgotPasswordFragment,
                    bundleOf("username" to viewModel.formFields.username)
                )
            } else if (status === ForgotPasswordStatus.INTERNAL_ERROR) {
                binding.textViewRecoverPasswordError.text = getString(R.string.sendCodeError)
            }

        }

    }

    private fun clearError() {
        binding.apply {
            textInputUsername.error = null
        }

    }


}