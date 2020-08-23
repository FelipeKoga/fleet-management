
package co.tcc.koga.android.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import co.tcc.koga.android.ui.MainActivity
import co.tcc.koga.android.R
import co.tcc.koga.android.databinding.LoginActivityBinding
import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.login_activity)
        binding.lifecycleOwner = this
        viewModel.getContacts()
        binding.apply {
            buttonLogin.setOnClickListener {
                viewModel.authenticate(
                    binding.textInputLogin.text.toString(),
                    binding.textInputPassword.text.toString()
                )
            }
        }

        viewModel.loadingSignIn.observe(this, Observer {  isLoading ->
            buttonLogin.visibility = if (isLoading) View.GONE else View.VISIBLE
            progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE

        })

        viewModel.signInStatus.observe(this, Observer { isSignIn ->
            if (isSignIn) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }
        })
    }
}
