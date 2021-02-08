package co.tcc.koga.android.ui.splash_screen

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import javax.inject.Inject

class SplashScreenFragment : Fragment(R.layout.splash_screen_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<SplashScreenViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         viewModel.initApp()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.appStatus.observe(viewLifecycleOwner, { status ->
            if (!status) {
                showErrorToast()
            }
        })

        viewModel.isLogged.observe(viewLifecycleOwner, { isLogged ->
            if (isLogged) {
                findNavController().navigate(
                    R.id.action_splashScreenFragment_to_chatsFragment,
                )
            } else {
                findNavController().navigate(
                    R.id.action_splashScreenFragment_to_loginFragment,
                )
            }

        })
    }

    private fun showErrorToast() {
        Toast.makeText(requireContext(), "Erro ao inicializar o app.", Toast.LENGTH_LONG)
            .show()
    }

}