package com.t3ddyss.clother.presentation.auth.recovery

import com.t3ddyss.clother.domain.auth.AuthInteractor

sealed interface PasswordRecoveryState {
    object Loading : PasswordRecoveryState
    data class ValidationError(val causes: List<AuthInteractor.AuthParam>) : PasswordRecoveryState
    object Success : PasswordRecoveryState
    object Error : PasswordRecoveryState
}