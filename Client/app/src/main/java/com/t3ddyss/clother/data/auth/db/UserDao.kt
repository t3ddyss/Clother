package com.t3ddyss.clother.data.auth.db

import androidx.room.*
import com.t3ddyss.clother.data.auth.db.models.UserDetailsEntity
import com.t3ddyss.clother.data.auth.db.models.UserEntity
import com.t3ddyss.clother.data.auth.db.models.UserWithDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userDetails: UserDetailsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("DELETE FROM user")
    suspend fun deleteAll()

    @Query("SELECT * FROM user WHERE id == :id")
    suspend fun getUserById(id: Int): UserEntity

    @Transaction
    @Query(USER_WITH_DETAILS_QUERY)
    suspend fun getUserWithDetailsById(id: Int): UserWithDetailsEntity?

    @Transaction
    @Query(USER_WITH_DETAILS_QUERY)
    fun observeUserWithDetailsById(id: Int): Flow<UserWithDetailsEntity>

    private companion object {
        const val USER_WITH_DETAILS_QUERY = """
            SELECT *
            FROM user
            INNER JOIN user_details
            ON user.id == user_details.user_id
            WHERE user.id == :id
            """
    }
}