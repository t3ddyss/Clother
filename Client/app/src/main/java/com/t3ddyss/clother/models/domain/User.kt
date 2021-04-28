package com.t3ddyss.clother.models.domain

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val image: String?
)