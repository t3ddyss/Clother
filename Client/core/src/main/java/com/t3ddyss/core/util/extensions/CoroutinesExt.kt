package com.t3ddyss.core.util.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

fun Throwable.rethrowIfCancellationException() {
    if (this is CancellationException) throw this
}

inline fun <T, R> Flow<List<T>>.mapList(crossinline mapper: (T) -> R): Flow<List<R>> {
    return this.map { it.map(mapper) }
}

context(Fragment)
inline fun <T> Flow<T>.collectViewLifecycleAware(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend (T) -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        flowWithLifecycle(viewLifecycleOwner.lifecycle, minActiveState)
            .collect {
                block(it)
            }
    }
}