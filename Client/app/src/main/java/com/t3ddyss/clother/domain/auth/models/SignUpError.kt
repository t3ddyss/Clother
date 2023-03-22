package com.t3ddyss.clother.domain.auth.models

import com.t3ddyss.core.domain.models.ApiCallError

sealed interface SignUpError {
    object EmailOccupied : SignUpError
    data class Common(val error: ApiCallError) : SignUpError
}