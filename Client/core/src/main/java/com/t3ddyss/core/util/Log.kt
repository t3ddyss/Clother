package com.t3ddyss.core.util

import android.util.Log

private const val DEBUG_TAG = "ClotherLogs"

fun log(message: String, tag: String = DEBUG_TAG) {
    Log.d(tag, message)
}