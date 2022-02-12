package com.t3ddyss.core.util

import android.util.TypedValue
import androidx.fragment.app.Fragment

val Fragment.dp: Int.() -> Float
    get() = {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            this@dp.resources.displayMetrics
        )
    }