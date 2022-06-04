package com.t3ddyss.clother.utils

import androidx.annotation.StringRes
import androidx.test.platform.app.InstrumentationRegistry

object EspressoUtil {
    fun getString(@StringRes stringRes: Int): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return context.getString(stringRes)
    }
}