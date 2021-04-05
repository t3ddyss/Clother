package com.t3ddyss.clother.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.CategoryDao
import com.t3ddyss.clother.db.OfferDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.utilities.AUTH
import com.t3ddyss.clother.utilities.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.annotation.Signed
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // TODO implement migration
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .createFromAsset("clother_category.db")
                .fallbackToDestructiveMigration()
                .build()
    }

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

    @Signed
    @Provides
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(AUTH, Context.MODE_PRIVATE)
}