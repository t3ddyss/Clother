package com.t3ddyss.core.util

import android.os.Build
import com.t3ddyss.core.BuildConfig

object Utils {
    val isDebug = BuildConfig.DEBUG
    val isEmulator = Build.MODEL.contains("sdk_gphone")

    val Any.asExpression
        get() = Unit
}