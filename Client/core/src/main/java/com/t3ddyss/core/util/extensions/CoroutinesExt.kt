package com.t3ddyss.core.util.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

fun Throwable.rethrowIfCancellationException() {
    if (this is CancellationException) throw this
}

inline fun <T, R> Flow<List<T>>.nestedMap(crossinline mapper: (T) -> R): Flow<List<R>> {
    return this.map { it.map(mapper) }
}