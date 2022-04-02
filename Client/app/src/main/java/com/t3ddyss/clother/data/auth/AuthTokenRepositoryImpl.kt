package com.t3ddyss.clother.data.auth

import android.content.SharedPreferences
import com.t3ddyss.clother.data.Mappers.toDomain
import com.t3ddyss.clother.data.remote.RemoteAuthService
import com.t3ddyss.clother.data.remote.dto.AuthDataDto
import com.t3ddyss.clother.domain.auth.AuthTokenRepository
import com.t3ddyss.clother.util.REFRESH_TOKEN
import com.t3ddyss.clother.util.toBearer
import com.t3ddyss.core.util.log
import dagger.Lazy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenRepositoryImpl @Inject constructor(
    private val remoteAuthServiceLazy: Lazy<RemoteAuthService>,
    private val prefs: SharedPreferences
) : AuthTokenRepository {
    private val remoteAuthService get() = remoteAuthServiceLazy.get()

    private val _tokenStateFlow: MutableStateFlow<AuthDataDto?> = MutableStateFlow(null)
    override val tokenStateFlow = _tokenStateFlow
        .drop(1)
        .map { it?.toDomain() }

    override fun authenticate(route: Route?, response: Response): Request? {
        response.request.url.pathSegments.let {
            if (it.containsAll(listOf("api", "auth", "refresh")) && it.size == 3) {
                return unauthorized()
            }
        }
        log("Requesting auth tokens...")

        val refreshToken = prefs.getString(REFRESH_TOKEN, null) ?: return unauthorized()
        val refreshResponse = try {
            runBlocking {
                remoteAuthService.refreshTokens(refreshToken)
            }
        } catch (ex: Exception) {
            null
        }
        val authData = refreshResponse?.body() ?: return unauthorized()
        log("Requesting new auth tokens: success")

        _tokenStateFlow.value = authData
        return response
            .request
            .newBuilder()
            .header("Authorization", authData.accessToken.toBearer())
            .build()
    }

    private fun unauthorized(): Request? {
        log("Requesting new auth tokens: refresh token expired")
        _tokenStateFlow.value = null
        return null
    }
}