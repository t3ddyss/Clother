package com.t3ddyss.clother.di.common.common

import android.content.Context
import androidx.room.Room
import com.t3ddyss.clother.data.auth.db.UserDao
import com.t3ddyss.clother.data.chat.db.ChatDao
import com.t3ddyss.clother.data.chat.db.MessageDao
import com.t3ddyss.clother.data.common.common.db.AppDatabase
import com.t3ddyss.clother.data.offers.db.CategoryDao
import com.t3ddyss.clother.data.offers.db.OfferDao
import com.t3ddyss.clother.data.offers.db.RemoteKeyDao
import com.t3ddyss.feature_location.data.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DATABASE_NAME = "Clother.db"

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room
            .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .createFromAsset("clother.db")
            .build()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object DaoModule {

        @Singleton
        @Provides
        fun provideOfferDao(appDatabase: AppDatabase): OfferDao {
            return appDatabase.offerDao()
        }

        @Singleton
        @Provides
        fun provideRemoteKeyDao(appDatabase: AppDatabase): RemoteKeyDao {
            return appDatabase.remoteKeyDao()
        }

        @Singleton
        @Provides
        fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
            return appDatabase.categoryDao()
        }

        @Singleton
        @Provides
        fun provideChatDao(appDatabase: AppDatabase): ChatDao {
            return appDatabase.chatDao()
        }

        @Singleton
        @Provides
        fun provideMessageDao(appDatabase: AppDatabase): MessageDao {
            return appDatabase.messageDao()
        }

        @Singleton
        @Provides
        fun provideUserDao(appDatabase: AppDatabase): UserDao {
            return appDatabase.userDao()
        }

        @Singleton
        @Provides
        fun provideLocationDao(appDatabase: AppDatabase): LocationDao {
            return appDatabase.locationDao()
        }
    }
}