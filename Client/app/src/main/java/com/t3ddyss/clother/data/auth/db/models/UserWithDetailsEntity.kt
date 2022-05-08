package com.t3ddyss.clother.data.auth.db.models

import androidx.room.Embedded

data class UserWithDetailsEntity(
    @Embedded
    val user: UserEntity,
    @Embedded
    val details: UserDetailsEntity
)
