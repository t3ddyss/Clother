package com.t3ddyss.clother.presentation.auth.signup

import com.t3ddyss.clother.domain.auth.AuthInteractor

sealed interface SignUpState {
    object Loading : SignUpState
    data class ValidationError(val causes: List<AuthInteractor.AuthParam>) : SignUpState
    object Error : SignUpState
    object Success : SignUpState
}