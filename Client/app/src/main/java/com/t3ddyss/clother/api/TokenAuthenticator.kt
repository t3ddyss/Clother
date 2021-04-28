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
    private val prefs: SharedPreferences
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d(DEBUG_TAG, "Need new access token")

        val service = lazyService.get() ?: return null
        val refreshToken = prefs.getString(REFRESH_TOKEN, null) ?: return null
        prefs.edit().remove(REFRESH_TOKEN).apply()

        val refreshResponse = try {
            runBlocking {
                service.refreshTokens(refreshToken)
            }
        } catch (ex: Exception) {
            null
        }

        val tokens = refreshResponse?.body() ?: return null
        prefs.edit().putString(ACCESS_TOKEN, "Bearer ${tokens.accessToken}").apply()
        prefs.edit().putString(REFRESH_TOKEN, "Bearer ${tokens.refreshToken}").apply()

        Log.d(DEBUG_TAG, "Retrying request...")

        return response
            .request
            .newBuilder()
            .header("Authorization", "Bearer ${tokens.accessToken}")
            .build()
    }
}