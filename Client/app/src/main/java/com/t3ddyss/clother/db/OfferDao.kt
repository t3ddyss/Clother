package com.t3ddyss.clother.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.models.entity.OfferEntity

@Dao
interface OfferDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<OfferEntity>)

    @Query("SELECT * FROM offer WHERE list_key == :listKey ORDER BY id DESC")
    fun getAllOffersByList(listKey: String): PagingSource<Int, OfferEntity>

    @Query("SELECT * FROM offer WHERE id == :id")
    suspend fun getOfferById(id: Int): OfferEntity

    @Query("DELETE FROM offer WHERE list_key == :listKey")
    suspend fun deleteAllOffersFromList(listKey: String)

    @Query("DELETE FROM offer WHERE id == :id")
    suspend fun deleteOfferById(id: Int)
}