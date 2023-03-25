package com.t3ddyss.clother.data.auth.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "user_details",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("user_id")
    )],
    indices = [Index(value = ["user_id"], unique = true)]
)
data class UserDetailsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_details_id")
    val id: Int = 0,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    val email: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Date,

    val status: String
)
