package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.models.domain.AuthState
import com.t3ddyss.clother.remote.RemoteAuthService
import com.t3ddyss.clother.util.ACCESS_TOKEN
import com.t3ddyss.clother.util.CURRENT_USER_ID
import com.t3ddyss.clother.util.REFRESH_TOKEN
import com.t3ddyss.clother.util.toBearer
import com.t3ddyss.core.util.log
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val serviceLazy: Lazy<RemoteAuthService>,
    private val authStateObserver: AuthStateObserver,
    private val prefs: SharedPreferences
) : Authenticator {
    private val service get() = serviceLazy.get()

    override fun authenticate(route: Route?, response: Response): Request? {
        response.request.url.pathSegments.let {
            if (it.containsAll(listOf("api", "auth", "refresh")) && it.size == 3) {
                return unauthorized()
            }
        }

        log("Need new access token")
        authStateObserver.authState.value = AuthState.Refreshing

        val refreshToken = prefs.getString(REFRESH_TOKEN, null) ?: return unauthorized()
        val refreshResponse = try {
            runBlocking {
                service.refreshTokens(refreshToken)
            }
        } catch (ex: Exception) {
            null
        }

        val authData = refreshResponse?.body() ?: return unauthorized()
        prefs.edit().putString(ACCESS_TOKEN, authData.accessToken.toBearer()).apply()
        prefs.edit().putString(REFRESH_TOKEN, authData.refreshToken.toBearer()).apply()
        authStateObserver.authState.tryEmit(AuthState.Authenticated(
            authData.accessToken.toBearer(),
            authData.user.id)
        )

        return response
            .request
            .newBuilder()
            .header("Authorization", authData.accessToken.toBearer())
            .build()
    }

    private fun unauthorized(): Request? {
        prefs.edit().remove(ACCESS_TOKEN).apply()
        prefs.edit().remove(REFRESH_TOKEN).apply()
        prefs.edit().remove(CURRENT_USER_ID).apply()
        authStateObserver.authState.value = AuthState.None
        return null
    }
}