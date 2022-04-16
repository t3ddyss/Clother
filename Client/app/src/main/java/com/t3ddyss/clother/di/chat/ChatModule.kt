package com.t3ddyss.clother.di.chat

import com.t3ddyss.clother.data.chat.ChatRepositoryImpl
import com.t3ddyss.clother.domain.chat.ChatInteractor
import com.t3ddyss.clother.domain.chat.ChatInteractorImpl
import com.t3ddyss.clother.domain.chat.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ChatModule {
    @Singleton
    @Binds
    fun ChatInteractorImpl.bindChatInteractor(): ChatInteractor

    @Singleton
    @Binds
    fun ChatRepositoryImpl.bindChatRepository(): ChatRepository
}