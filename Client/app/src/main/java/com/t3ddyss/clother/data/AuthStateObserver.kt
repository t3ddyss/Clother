package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.models.domain.AuthState
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthStateObserver @Inject constructor(
    private val prefs: SharedPreferences
) {
    val authState by lazy {
        val initialState = prefs.getString(ACCESS_TOKEN, null).let {
            if (it == null) AuthState.None
            else AuthState.Authenticated(it)
        }
        MutableStateFlow(initialState)
    }
}