package com.t3ddyss.clother.domain.auth

import com.t3ddyss.clother.domain.auth.models.AuthData
import com.t3ddyss.clother.domain.auth.models.AuthState
import com.t3ddyss.clother.util.handleHttpException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class AuthInteractorImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val authTokenRepository: AuthTokenRepository
) : AuthInteractor {

    init {
        observeTokenState()
    }

    override val authState by lazy {
        MutableStateFlow(authRepository.authData.toAuthState())
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String
    ) = handleHttpException {
        authRepository.signUp(name, email, password)
    }

    override suspend fun signIn(
        email: String,
        password: String
    ) = handleHttpException {
        val authData = authRepository.signIn(email, password)
        authRepository.saveAuthData(authData)
        authState.value = authData.toAuthState()
        authData
    }

    override suspend fun resetPassword(email: String) = handleHttpException {
        authRepository.resetPassword(email)
    }

    private fun observeTokenState() {
        authTokenRepository.tokenStateFlow
            .map { it.toAuthState() }
            .onEach {
                when (it) {
                    is AuthState.None -> authRepository.deleteAuthData()
                    is AuthState.Authenticated -> authRepository.saveAuthData(it.authData)
                }
                authState.value = it
            }
    }

    private fun AuthData?.toAuthState() = if (this?.accessToken == null) {
        AuthState.None
    } else {
        AuthState.Authenticated(this)
    }
}