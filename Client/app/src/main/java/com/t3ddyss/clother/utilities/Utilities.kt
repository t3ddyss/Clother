package com.t3ddyss.clother.utilities

import android.os.Build
import kotlin.math.abs
import kotlin.math.floor

fun isEmulator() = Build.FINGERPRINT.contains("generic")

fun getBaseUrlForCurrentDevice() = if (isEmulator()) BASE_URL_EMULATOR else BASE_URL_DEVICE

fun convertToDms(coordinate: Double): String {
    val absolute = abs(coordinate)
    val degrees = floor(absolute).toInt()
    val minutesNotTruncated = (absolute - degrees) * 60
    val minutes = floor(minutesNotTruncated).toInt()
    val seconds = floor((minutesNotTruncated - minutes) * 60).toInt()
    return "$degrees°$minutes'$seconds\""
}