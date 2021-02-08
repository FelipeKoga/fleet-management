package co.tcc.koga.android.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import co.tcc.koga.android.App
import co.tcc.koga.android.R
import co.tcc.koga.android.ui.di.MainComponent
import co.tcc.koga.android.worker.UpdateLocationWorker
import javax.inject.Inject


class MainActivity : AppCompatActivity() {
    lateinit var mainComponent: MainComponent
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MainViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        mainComponent = (applicationContext as App).appComponent.mainComponent().create()
        mainComponent.inject(this)

        setTheme(R.style.SplashScreenTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        UpdateLocationWorker.schedule(application)
        viewModel.observeAuthStatus()
    }
}