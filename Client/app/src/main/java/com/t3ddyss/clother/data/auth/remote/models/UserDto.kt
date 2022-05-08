package com.t3ddyss.clother.data.auth.remote.models

data class UserDto(
    val id: Int,
    val name: String,
    val image: String?,
    val details: UserDetailsDto?
)
