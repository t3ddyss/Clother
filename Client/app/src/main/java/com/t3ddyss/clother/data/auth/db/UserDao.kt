package com.t3ddyss.clother.data.auth.db

import androidx.room.*
import com.t3ddyss.clother.data.auth.db.models.UserDetailsEntity
import com.t3ddyss.clother.data.auth.db.models.UserEntity
import com.t3ddyss.clother.data.auth.db.models.UserWithDetailsEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userDetails: UserDetailsEntity)

    @Query("DELETE FROM user")
    suspend fun deleteAll()

    @Transaction
    @Query("""SELECT * 
        FROM user 
        INNER JOIN user_details
        ON user.id == user_details_id
        WHERE user.id = :id
    """
    )
    suspend fun getUserWithDetailsById(id: Int): UserWithDetailsEntity
}