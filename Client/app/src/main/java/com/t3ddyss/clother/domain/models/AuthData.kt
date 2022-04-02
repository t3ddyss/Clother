package com.t3ddyss.clother.domain.models

import com.t3ddyss.core.domain.models.User

data class AuthData(
    val user: User,
    val accessToken: String?,
    val refreshToken: String?
)