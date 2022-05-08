package com.t3ddyss.clother.domain.auth.models

data class UserAuthData(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)
