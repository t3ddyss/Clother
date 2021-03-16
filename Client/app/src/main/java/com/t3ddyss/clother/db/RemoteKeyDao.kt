package com.t3ddyss.clother.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.models.RemoteKey

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: RemoteKey)

    @Query("SELECT * FROM remote_keys WHERE list == :list")
    suspend fun remoteKeyByList(list: String): RemoteKey

    @Query("DELETE FROM remote_keys WHERE list == :list")
    suspend fun removeByList(list: String)
}