package dev.msemyak.geocam.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dev.msemyak.geocam.di.scopes_and_qualifiers.ApplicationContextQualifier
import dev.msemyak.geocam.di.scopes_and_qualifiers.CameraApplicationScope

@Module
class ContextModule(var context: Context) {

    @Provides
    @CameraApplicationScope
    @ApplicationContextQualifier
    fun provideContext():Context = context

}