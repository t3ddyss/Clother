package com.t3ddyss.clother.data.offers.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.data.offers.db.models.RemoteKeyEntity

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: RemoteKeyEntity)

    @Query("SELECT * FROM remote_key WHERE list == :list")
    suspend fun remoteKeyByList(list: String): RemoteKeyEntity

    @Query("DELETE FROM remote_key WHERE list == :list")
    suspend fun removeByList(list: String)
}