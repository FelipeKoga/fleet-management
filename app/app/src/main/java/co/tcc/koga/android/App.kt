package co.tcc.koga.android

import android.app.Application
import co.tcc.koga.android.di.ApplicationComponent
import co.tcc.koga.android.di.DaggerApplicationComponent

class App : Application() {

    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerApplicationComponent.factory().create(this)
    }
}