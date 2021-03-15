package com.t3ddyss.clother.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.models.Offer

@Dao
interface OfferDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOffers(offers: List<Offer>)

    @Query("SELECT * FROM offers")
    suspend fun getAllOffers(): List<Offer>

    @Query("DELETE FROM offers")
    suspend fun deleteAllOffers()
}