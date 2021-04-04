package com.t3ddyss.clother.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.models.Offer

@Dao
interface OfferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<Offer>)

    // No suspend modifier because this function is called from repository's function
    // which is always called within the coroutine
    @Query("SELECT * FROM offer ORDER BY id DESC")
    fun getAllOffers(): PagingSource<Int, Offer>

    @Query("DELETE FROM offer")
    suspend fun deleteAllOffers()
}