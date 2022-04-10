package com.t3ddyss.clother.data.auth.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.data.auth.db.models.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Query("DELETE FROM USER")
    suspend fun deleteAll()

    @Query("SELECT * FROM USER WHERE id = :id")
    suspend fun getCurrentUser(id: Int): UserEntity
}