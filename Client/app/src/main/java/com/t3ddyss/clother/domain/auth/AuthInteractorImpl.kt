package com.t3ddyss.clother.domain.auth

import com.t3ddyss.clother.data.common.common.Mappers.toAuthData
import com.t3ddyss.clother.domain.auth.models.AuthData
import com.t3ddyss.clother.domain.auth.models.AuthState
import com.t3ddyss.clother.domain.auth.models.UserAuthData
import com.t3ddyss.clother.util.DispatchersProvider
import com.t3ddyss.clother.util.handleHttpException
import com.t3ddyss.core.util.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthInteractorImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val authTokenRepository: AuthTokenRepository,
    private val scope: CoroutineScope,
    private val dispatchers: DispatchersProvider
) : AuthInteractor {

    override val authStateFlow by lazy {
        MutableStateFlow(authRepository.authData.toAuthState())
    }

    override fun initialize() {
        observeTokenState()
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
        val userAuthData = authRepository.signIn(email, password)
        authRepository.saveUserAuthData(userAuthData)
        authStateFlow.value = userAuthData.toAuthState()
        userAuthData
    }

    override suspend fun resetPassword(email: String) = handleHttpException {
        authRepository.resetPassword(email)
    }

    private fun observeTokenState() = scope.launch(dispatchers.io) {
        authTokenRepository.tokenStateFlow
            .onEach {
                val authState = it.toAuthState()
                when (authState) {
                    is AuthState.None -> authRepository.deleteAllUserData()
                    is AuthState.Authenticated -> authRepository.saveUserAuthData(it!!)
                }
                authStateFlow.tryEmit(authState)
                log("AuthInteractorImpl.observeTokenState() $authState")
            }.collect()
    }

    private fun AuthData?.toAuthState() = if (this?.accessToken == null) {
        AuthState.None
    } else {
        AuthState.Authenticated(this)
    }

    private fun UserAuthData?.toAuthState() = if (this == null) {
        AuthState.None
    } else {
        AuthState.Authenticated(this.toAuthData())
    }
}