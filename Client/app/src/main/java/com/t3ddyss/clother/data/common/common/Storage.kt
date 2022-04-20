package com.t3ddyss.clother.data.common.common

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Storage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
    }

    var accessToken: String?
        get() = prefs.getString(ACCESS_TOKEN, null)
        set(value) {
            prefs.edit().putString(ACCESS_TOKEN, value).apply()
        }
    var refreshToken: String?
        get() = prefs.getString(REFRESH_TOKEN, null)
        set(value) {
            prefs.edit().putString(REFRESH_TOKEN, value).apply()
        }
    var userId: Int
        get() = prefs.getInt(USER_ID, 0)
        set(value) {
            prefs.edit().putInt(USER_ID, value).apply()
        }
    var isDeviceTokenRetrieved: Boolean
        get() = prefs.getBoolean(IS_DEVICE_TOKEN_RETRIEVED, false)
        set(value) {
            prefs.edit().putBoolean(IS_DEVICE_TOKEN_RETRIEVED, value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val AUTH_PREFS_NAME = "auth_prefs"
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val USER_ID = "user_id"
        const val IS_DEVICE_TOKEN_RETRIEVED = "is_device_token_retrieved"
    }
}