package com.t3ddyss.feature_location.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long

    @Query("SELECT * FROM location ORDER BY id DESC LIMIT 1")
    suspend fun getLatestLocation(): LocationEntity?
}