package com.t3ddyss.clother.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String?,
    val image: String?
)