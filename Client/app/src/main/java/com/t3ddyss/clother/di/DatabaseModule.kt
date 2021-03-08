package com.t3ddyss.clother.di

import android.content.Context
import android.content.SharedPreferences
import com.t3ddyss.clother.utilities.AUTH
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(AUTH, Context.MODE_PRIVATE)
}