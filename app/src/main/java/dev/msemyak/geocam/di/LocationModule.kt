package dev.msemyak.geocam.di

import android.content.Context
import com.patloew.rxlocation.RxLocation
import dagger.Module
import dagger.Provides
import dev.msemyak.geocam.di.scopes_and_qualifiers.ApplicationContextQualifier
import dev.msemyak.geocam.di.scopes_and_qualifiers.CameraApplicationScope

@Module(includes = [ContextModule::class])
class LocationModule {

    @Provides
    @CameraApplicationScope
    fun provideRxLocation(@ApplicationContextQualifier context: Context): RxLocation = RxLocation(context)

}