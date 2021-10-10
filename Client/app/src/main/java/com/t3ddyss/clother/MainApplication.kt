package com.t3ddyss.clother

import android.app.Application
import com.t3ddyss.clother.di.Initializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var initializer: Initializer

    override fun onCreate() {
        super.onCreate()
        initializer.initialize()
    }
}