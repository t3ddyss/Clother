package com.t3ddyss.clother.api

import android.content.SharedPreferences
import android.util.Log
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.DEBUG_TAG
import com.t3ddyss.clother.utilities.REFRESH_TOKEN
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val lazyService: Lazy<ClotherAuthService>,
    private val prefs: SharedPreferences)
: Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val service = lazyService.get() ?: return null
        val refreshToken = prefs.getString(REFRESH_TOKEN, null)
                ?: throw IllegalArgumentException()

        val tokens = runBlocking {
            service.refreshTokens("Bearer $refreshToken")
        }

        Log.d(DEBUG_TAG, "Got updated tokens")

        prefs.edit().putString(ACCESS_TOKEN, tokens.accessToken).apply()
        prefs.edit().putString(REFRESH_TOKEN, tokens.refreshToken).apply()

        return response
            .request
            .newBuilder()
            .header("Authorization", "Bearer ${tokens.accessToken}")
            .build()
    }
}