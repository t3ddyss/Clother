package com.t3ddyss.clother.presentation

import android.app.Application
import com.t3ddyss.clother.domain.InitializationInteractor
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var initializationInteractor: InitializationInteractor

    override fun onCreate() {
        super.onCreate()
        initializationInteractor.initialize()
    }
}