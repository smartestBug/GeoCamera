package dev.msemyak.geocam

import android.app.Application
import com.patloew.rxlocation.RxLocation
import dev.msemyak.geocam.di.CameraComponent
import dev.msemyak.geocam.di.CameraModule
import dev.msemyak.geocam.di.DaggerCameraComponent

class AppBoss : Application() {
    init {
        appInstance = this
    }

    companion object {
        private var appInstance: AppBoss? = null
        var cameraComponent: CameraComponent? = null
        lateinit var rxLocation: RxLocation

    }

    override fun onCreate() {
        super.onCreate()

        cameraComponent = DaggerCameraComponent.builder()
                .cameraModule(CameraModule())
                .build()

        rxLocation = RxLocation(this)

    }
}