package dev.msemyak.geocam.di

import dagger.Module
import dagger.Provides
import dev.msemyak.geocam.ui.camera.CameraContract
import dev.msemyak.geocam.ui.camera.CameraPresenter
import javax.inject.Singleton

@Module
class CameraModule {

    @Provides
    @Singleton
    fun provideCameraPresenter(): CameraContract.Presenter = CameraPresenter()

}