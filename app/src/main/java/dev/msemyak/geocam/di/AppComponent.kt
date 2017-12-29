package dev.msemyak.geocam.di

import com.patloew.rxlocation.RxLocation
import dagger.Component
import dev.msemyak.geocam.di.scopes_and_qualifiers.CameraApplicationScope
import dev.msemyak.geocam.ui.camera.CameraContract

@CameraApplicationScope
@Component(modules = [CameraModule::class, LocationModule::class])

interface AppComponent {

    fun inject(cameraActivity: CameraContract.View)
    fun getCameraPresenter(): CameraContract.Presenter

    fun getRxLocation(): RxLocation
}

