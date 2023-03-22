package com.t3ddyss.core.util.extensions

import com.t3ddyss.core.R
import com.t3ddyss.core.domain.models.ApiCallError

val ApiCallError.textRes
    get() = when (this) {
        ApiCallError.ConnectionError -> R.string.error_no_connection
        ApiCallError.UnknownError -> R.string.error_unknown
    }