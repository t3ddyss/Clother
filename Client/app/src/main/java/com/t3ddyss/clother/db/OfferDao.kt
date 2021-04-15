package com.t3ddyss.clother.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.models.offers.Offer

@Dao
interface OfferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<Offer>)

    @Query("SELECT * FROM offer ORDER BY id DESC")
    fun getAllOffers(): PagingSource<Int, Offer>

    @Query("SELECT * FROM offer WHERE id == :id")
    suspend fun getOfferById(id: Int): Offer

    @Query("DELETE FROM offer")
    suspend fun deleteAllOffers()
}