package dev.msemyak.geocam.di

import dagger.Component
import dev.msemyak.geocam.ui.camera.CameraActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [(CameraModule::class)])

interface CameraComponent {

    fun inject(cameraPresenter: CameraActivity)

}

