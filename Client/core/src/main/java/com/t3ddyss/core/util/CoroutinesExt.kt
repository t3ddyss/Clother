package com.t3ddyss.core.util

import kotlin.coroutines.cancellation.CancellationException

fun Throwable.ignoreCancellationException() {
    if (this is CancellationException) throw this
}