package dev.msemyak.geocam

import android.app.Application
import dev.msemyak.geocam.di.*

class AppBoss : Application() {
    init {
        appInstance = this
    }

    companion object {
        private var appInstance: AppBoss? = null
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(this))
                .cameraModule(CameraModule())
                .locationModule(LocationModule())
                .build()
    }
}