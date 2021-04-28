package com.t3ddyss.clother.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.t3ddyss.clother.models.entity.*

@Database(
    entities = [OfferEntity::class, RemoteKeyEntity::class, CategoryEntity::class, ChatEntity::class,
        MessageEntity::class, UserEntity::class, LocationEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun offerDao(): OfferDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun categoryDao(): CategoryDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
}