package com.t3ddyss.clother.domain.auth.models

import com.t3ddyss.core.domain.models.ApiCallError

sealed interface UserInfoState {
    val user: User

    data class Cache(override val user: User) : UserInfoState
    data class Fetched(override val user: User) : UserInfoState
    data class Error(override val user: User, val error: ApiCallError) : UserInfoState
}