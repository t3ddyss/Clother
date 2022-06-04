package com.t3ddyss.clother.di

import android.content.Context
import androidx.room.Room
import com.t3ddyss.clother.data.common.common.db.AppDatabase
import com.t3ddyss.clother.di.common.common.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room
            .inMemoryDatabaseBuilder(
                context,
                AppDatabase::class.java)
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .allowMainThreadQueries()
            .build()
    }
}