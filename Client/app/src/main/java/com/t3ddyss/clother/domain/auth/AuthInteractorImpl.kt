package com.t3ddyss.clother.domain.auth

import arrow.core.*
import com.t3ddyss.clother.data.common.common.Mappers.toAuthData
import com.t3ddyss.clother.domain.auth.models.*
import com.t3ddyss.clother.util.DispatchersProvider
import com.t3ddyss.core.util.log
import com.t3ddyss.core.util.utils.StringUtils
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
    ): Either<SignUpError, Unit> {
        require(validateParameters(name, email, password).isRight())
        return authRepository.signUp(name, email, password)
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Either<SignInError, UserAuthData> {
        return authRepository.signIn(email, password)
            .tap { userAuthData ->
                authRepository.saveUserAuthData(userAuthData)
                authStateFlow.value = userAuthData.toAuthState()
            }
    }

    override suspend fun resetPassword(email: String): Either<ResetPasswordError, Unit> {
        return authRepository.resetPassword(email)
    }

    override suspend fun validateParameters(
        name: String?,
        email: String?,
        password: String?
    ): Either<Nel<AuthInteractor.AuthParam>, Unit> {
        return AuthInteractor.AuthParam.values().asList().traverse { param ->
            when (param) {
                AuthInteractor.AuthParam.NAME -> {
                    if (name == null || StringUtils.isValidName(name)) {
                        param.validNel()
                    } else {
                        param.invalidNel()
                    }
                }
                AuthInteractor.AuthParam.EMAIL -> {
                    if (email == null || StringUtils.isValidEmail(email)) {
                        param.validNel()
                    } else {
                        param.invalidNel()
                    }
                }
                AuthInteractor.AuthParam.PASSWORD -> {
                    if (password == null || StringUtils.isValidPassword(password)) {
                        param.validNel()
                    } else {
                        param.invalidNel()
                    }
                }
            }
        }
            .toEither()
            .void()
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