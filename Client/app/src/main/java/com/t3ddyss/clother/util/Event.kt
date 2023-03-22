package com.t3ddyss.clother.util

import java.util.concurrent.atomic.AtomicBoolean

data class Event<out T>(
    private val content: T
) {
    private val isHandled = AtomicBoolean()

    fun getContentOrNull(): T? {
        return if (isHandled.compareAndSet(false, true)) {
            content
        } else {
            null
        }
    }
}

fun <T> T.toEvent() = Event(this)