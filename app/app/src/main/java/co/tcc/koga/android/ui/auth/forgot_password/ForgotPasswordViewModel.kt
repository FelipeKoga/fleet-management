package co.tcc.koga.android.ui.auth.forgot_password

import android.content.Context
import androidx.lifecycle.ViewModel
import co.tcc.koga.android.data.repository.ClientRepository
import javax.inject.Inject

class ForgotPasswordViewModel @Inject constructor(
    private val repository: ClientRepository,
    private val context: Context
) :
    ViewModel() {

}