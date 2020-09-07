package co.tcc.koga.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.tcc.koga.android.data.network.websocket.Socket
import co.tcc.koga.android.ui.di.MainComponent

class MainActivity : AppCompatActivity() {

    lateinit var mainComponent: MainComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        mainComponent = (applicationContext as App).appComponent.mainComponent().create()
        setTheme(R.style.SplashScreenTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}