package com.t3ddyss.navigation.util

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

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