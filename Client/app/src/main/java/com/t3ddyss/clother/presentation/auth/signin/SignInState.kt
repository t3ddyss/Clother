package com.t3ddyss.clother.presentation.auth.signin

import com.t3ddyss.clother.domain.auth.AuthInteractor

sealed interface SignInState {
    object Loading : SignInState
    data class ValidationError(val causes: List<AuthInteractor.AuthParam>) : SignInState
    object Error : SignInState
    object Success : SignInState
}