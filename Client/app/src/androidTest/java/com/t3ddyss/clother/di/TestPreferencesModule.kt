package com.t3ddyss.clother.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PreferencesModule::class]
)
object TestPreferencesModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(): SharedPreferences {
        val prefs = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(prefs.getString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn("abc")

        return prefs
    }
}