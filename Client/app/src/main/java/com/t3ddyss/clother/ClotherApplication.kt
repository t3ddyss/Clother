package com.t3ddyss.clother

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp
import java.util.*

@HiltAndroidApp
class ClotherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        MapsInitializer.initialize(this)
    }
}