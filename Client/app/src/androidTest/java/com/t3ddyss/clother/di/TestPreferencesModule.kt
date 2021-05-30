package com.t3ddyss.clother.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.ArgumentMatchers.*
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
        val prefsEditor = Mockito.mock(SharedPreferences.Editor::class.java)

        Mockito
            .`when`(prefsEditor.putString(anyString(), anyString()))
            .thenReturn(prefsEditor)

        Mockito
            .`when`(prefsEditor.putInt(anyString(), anyInt()))
            .thenReturn(prefsEditor)

        Mockito
            .`when`(prefsEditor.putBoolean(anyString(), anyBoolean()))
            .thenReturn(prefsEditor)

        Mockito
            .doNothing()
            .`when`(prefsEditor).apply()

        Mockito
            .`when`(prefs.edit())
            .thenReturn(prefsEditor)

        Mockito
            .`when`(prefs.getString(anyString(), anyString()))
            .thenReturn("abcde")

        return prefs
    }
}