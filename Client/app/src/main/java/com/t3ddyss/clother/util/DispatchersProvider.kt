package com.t3ddyss.clother.util

import kotlinx.coroutines.CoroutineDispatcher

interface DispatchersProvider {
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
}