package com.t3ddyss.core.util.utils

import android.os.Build
import com.t3ddyss.core.BuildConfig

object Utils {
    val isDebug = BuildConfig.DEBUG
    val isEmulator = Build.MODEL.contains("sdk_gphone")
}