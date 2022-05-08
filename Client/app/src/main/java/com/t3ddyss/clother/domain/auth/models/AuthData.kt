package com.t3ddyss.clother.domain.auth.models

data class AuthData(
    val userId: Int,
    val accessToken: String?,
    val refreshToken: String?
)