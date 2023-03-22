package com.t3ddyss.clother.domain.auth.models

import com.t3ddyss.core.domain.models.ApiCallError

sealed interface ResetPasswordError {
    object UserNotFound : ResetPasswordError
    data class Common(val error: ApiCallError) : ResetPasswordError
}