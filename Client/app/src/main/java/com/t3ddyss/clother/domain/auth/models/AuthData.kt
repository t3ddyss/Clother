package com.t3ddyss.clother.domain.auth.models

import com.t3ddyss.core.domain.models.User

data class AuthData(
    val user: User,
    val accessToken: String?,
    val refreshToken: String?
)