package com.t3ddyss.clother.data.common.common.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.t3ddyss.clother.data.auth.db.UserDao
import com.t3ddyss.clother.data.auth.db.models.UserEntity
import com.t3ddyss.clother.data.chat.db.ChatDao
import com.t3ddyss.clother.data.chat.db.MessageDao
import com.t3ddyss.clother.data.chat.db.models.ChatEntity
import com.t3ddyss.clother.data.chat.db.models.MessageEntity
import com.t3ddyss.clother.data.offers.db.CategoryDao
import com.t3ddyss.clother.data.offers.db.OfferDao
import com.t3ddyss.clother.data.offers.db.RemoteKeyDao
import com.t3ddyss.clother.data.offers.db.models.CategoryEntity
import com.t3ddyss.clother.data.offers.db.models.OfferEntity
import com.t3ddyss.clother.data.offers.db.models.RemoteKeyEntity
import com.t3ddyss.feature_location.data.LocationDao
import com.t3ddyss.feature_location.data.LocationEntity

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