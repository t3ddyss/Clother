package com.t3ddyss.navigation.util

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.t3ddyss.core.util.observeOnce

fun <T : Any> Fragment.setNavigationResult(key: String, data : T) {
    findNavController()
        .previousBackStackEntry
        ?.savedStateHandle
        ?.set(key, data)
}

inline fun <T : Any> Fragment.observeNavigationResult(key: String, crossinline action: (T) -> (Unit)) {
    findNavController()
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<T>(key)
        ?.observe(viewLifecycleOwner) {
            action(it)
    }
}

inline fun <T : Any> Fragment.observeNavigationResultOnce(key: String, crossinline action: (T) -> (Unit)) {
    val savedStateHandle = findNavController()
        .currentBackStackEntry
        ?.savedStateHandle
    savedStateHandle
        ?.getLiveData<T>(key)
        ?.observeOnce(viewLifecycleOwner) {
            action(it)
            savedStateHandle.remove<T>(key)
        }
}