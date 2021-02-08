package co.tcc.koga.android.ui.auth.confirm_forgot_password

import co.tcc.koga.android.databinding.ConfirmForgotPasswordFragmentBinding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.ui.auth.ForgotPasswordStatus
import javax.inject.Inject


class ConfirmForgotPasswordFragment : Fragment(R.layout.confirm_forgot_password_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ConfirmForgotPasswordViewModel> { viewModelFactory }
    private lateinit var binding: ConfirmForgotPasswordFragmentBinding
    private val args: ConfirmForgotPasswordFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ConfirmForgotPasswordFragmentBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.username = args.username
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.formErrors.observe(viewLifecycleOwner, { errors ->
            clearError()
            for ((key, value) in errors) {
                if (key === "code") {
                    binding.textInputCode.error = value
                }
                if (key === "newPassword") {
                    binding.textInputPassword.error = value
                }
                if (key === "confirmNewPassword") {
                    binding.textInputConfirmPassword.error = value
                }

                if (key === "differentPasswords") {
                    binding.textViewConfirmForgotPasswordError.visibility = View.VISIBLE
                    binding.textViewConfirmForgotPasswordError.text = value
                }
            }
        })

        viewModel.passwordRecoverStatus.observe(viewLifecycleOwner, { status ->
            if (status === ForgotPasswordStatus.SUCCESS) {
                findNavController().navigate(R.id.action_confirmForgotPasswordFragment_to_chatsFragment)
            } else if (status === ForgotPasswordStatus.INTERNAL_ERROR) {
                binding.textViewConfirmForgotPasswordError.visibility = View.VISIBLE
            }
        })
    }

    private fun clearError() {
        binding.apply {
            textInputCode.error = null
            textInputCode.error = null
            textInputConfirmPassword.error = null
            textViewConfirmForgotPasswordError.visibility = View.GONE

        }

    }


}