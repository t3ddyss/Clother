package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.models.domain.AuthState
import com.t3ddyss.clother.util.ACCESS_TOKEN
import com.t3ddyss.clother.util.CURRENT_USER_ID
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthStateObserver @Inject constructor(
    private val prefs: SharedPreferences
) {
    val authState by lazy {
        val accessToken = prefs.getString(ACCESS_TOKEN, null)
        val userId: Int = prefs.getInt(CURRENT_USER_ID, 0)

        val initialState = if (accessToken == null) {
            AuthState.None
        }
        else {
            AuthState.Authenticated(accessToken, userId)
        }
        MutableStateFlow(initialState)
    }

    val currentUserId: Int get() {
        val authStateValue = authState.value

        return if (authStateValue is AuthState.Authenticated) {
            authStateValue.userId
        } else {
            0
        }
    }
}