package com.t3ddyss.clother.domain.auth.models

data class User(
    val id: Int,
    val name: String,
    val image: String = "",
    val details: UserDetails? = null
)