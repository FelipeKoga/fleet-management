package co.tcc.koga.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import co.tcc.koga.android.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findNavController(R.id.nav_host_fragment)
        // Socket().sendAction(ResquestSendMessage("Ola", "sendMessage"))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        window.statusBarColor = ContextCompat
            .getColor(this, R.color.primaryColor)
        println("ON BAKC PRESSED")
    }
}