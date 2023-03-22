package com.t3ddyss.clother.data.offers.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.data.offers.db.models.OfferEntity
import com.t3ddyss.clother.data.offers.db.models.OfferWithUserEntity

@Dao
interface OfferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<OfferEntity>)

    @Query("""SELECT *
        FROM offer
        INNER JOIN user
        ON offer.user_id == user.id
        WHERE offer.offer_id == :id
        LIMIT 1
    """)
    suspend fun getOfferById(id: Int): OfferWithUserEntity

    @Query("""SELECT * 
        FROM offer
        INNER JOIN user
        ON offer.user_id == user.id
        WHERE list_key == :listKey
        ORDER BY offer.offer_id
        DESC
    """)
    fun getAllOffersByList(listKey: String): PagingSource<Int, OfferWithUserEntity>

    @Query("DELETE FROM offer WHERE list_key == :listKey")
    suspend fun deleteAllOffersFromList(listKey: String)

    @Query("DELETE FROM offer WHERE offer_id == :id")
    suspend fun deleteOfferById(id: Int)
}