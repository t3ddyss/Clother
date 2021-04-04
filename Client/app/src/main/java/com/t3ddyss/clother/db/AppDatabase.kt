package com.t3ddyss.clother.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.t3ddyss.clother.models.Category
import com.t3ddyss.clother.models.Offer
import com.t3ddyss.clother.models.RemoteKey

@Database(
        entities = [Offer::class, RemoteKey::class, Category::class],
        version = 1,
        exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun offerDao(): OfferDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun categoryDao(): CategoryDao
}