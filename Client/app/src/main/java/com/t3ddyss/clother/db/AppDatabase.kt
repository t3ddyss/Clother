package com.t3ddyss.clother.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.t3ddyss.clother.models.chat.Chat
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.models.offers.Category
import com.t3ddyss.clother.models.offers.Offer
import com.t3ddyss.clother.models.common.RemoteKey
import com.t3ddyss.clother.models.user.User

@Database(
        entities = [Offer::class, RemoteKey::class, Category::class, Chat::class,
                   Message::class, User::class],
        version = 4,
        exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun offerDao(): OfferDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun categoryDao(): CategoryDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}