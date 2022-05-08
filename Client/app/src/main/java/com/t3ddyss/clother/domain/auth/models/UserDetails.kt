package com.t3ddyss.clother.domain.auth.models

import java.util.*

data class UserDetails(
    val email: String,
    val createdAt: Date,
    val age: Int?
)
