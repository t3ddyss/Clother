package com.t3ddyss.clother.domain.auth.models

import com.t3ddyss.core.domain.models.ApiCallError

sealed interface SignInError {
    object EmailNotVerified : SignInError
    object InvalidCredentials : SignInError
    data class Common(val error: ApiCallError) : SignInError
}