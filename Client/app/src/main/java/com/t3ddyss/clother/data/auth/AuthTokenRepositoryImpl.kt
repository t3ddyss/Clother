package com.t3ddyss.clother.data.auth

import com.t3ddyss.clother.data.auth.remote.RemoteAuthService
import com.t3ddyss.clother.data.auth.remote.models.UserAuthDataDto
import com.t3ddyss.clother.data.common.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.domain.auth.AuthTokenRepository
import com.t3ddyss.clother.util.extensions.toBearer
import com.t3ddyss.core.util.log
import dagger.Lazy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenRepositoryImpl @Inject constructor(
    private val remoteAuthServiceLazy: Lazy<RemoteAuthService>,
    private val storage: Storage
) : AuthTokenRepository, Authenticator {
    private val remoteAuthService get() = remoteAuthServiceLazy.get()

    private val _tokenStateFlow: MutableSharedFlow<UserAuthDataDto?> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val tokenStateFlow = _tokenStateFlow
        .map { it?.toDomain() }

    override fun authenticate(route: Route?, response: Response): Request? {
        response.request.url.pathSegments.let {
            if (it.containsAll(listOf("api", "auth", "refresh")) && it.size == 3) {
                return unauthorized()
            }
        }

        synchronized(this) {
            val token = response.request.header(AUTHORIZATION)
            if (storage.accessToken != token) {
                log("AuthTokenRepositoryImpl.authenticate(). Using previously refreshed token")
                return response
                    .request
                    .newBuilder()
                    .header(AUTHORIZATION, storage.accessToken ?: "")
                    .build()
            }

            log("AuthTokenRepositoryImpl.authenticate(). Requesting auth tokens...")
            val refreshToken = storage.refreshToken ?: return unauthorized()
            val refreshResponse = try {
                runBlocking {
                    remoteAuthService.refreshTokens(refreshToken)
                }
            } catch (ex: Exception) {
                null
            }
            val authData = refreshResponse?.body() ?: return unauthorized()
            saveTokens(authData)
            _tokenStateFlow.tryEmit(authData)
            log("AuthTokenRepositoryImpl.authenticate(). Requesting auth tokens: success")

            return response
                .request
                .newBuilder()
                .header(AUTHORIZATION, authData.accessToken.toBearer())
                .build()
        }
    }

    private fun unauthorized(): Request? {
        log("AuthTokenRepositoryImpl.authenticate(). Requesting auth tokens: refresh token expired")
        _tokenStateFlow.tryEmit(null)
        return null
    }

    private fun saveTokens(authData: UserAuthDataDto) {
        storage.accessToken = authData.accessToken.toBearer()
        storage.refreshToken = authData.refreshToken.toBearer()
    }

    private companion object {
        const val AUTHORIZATION = "Authorization"
    }
}