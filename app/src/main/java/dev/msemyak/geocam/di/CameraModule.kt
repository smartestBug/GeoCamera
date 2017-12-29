package dev.msemyak.geocam.di

import dagger.Module
import dagger.Provides
import dev.msemyak.geocam.di.scopes_and_qualifiers.CameraApplicationScope
import dev.msemyak.geocam.ui.camera.CameraContract
import dev.msemyak.geocam.ui.camera.CameraPresenter

@Module
class CameraModule {

    @Provides
    @CameraApplicationScope
    fun provideCameraPresenter(): CameraContract.Presenter = CameraPresenter()

}