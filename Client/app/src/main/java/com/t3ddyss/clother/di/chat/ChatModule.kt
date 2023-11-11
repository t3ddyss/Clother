package com.t3ddyss.clother.di.chat

import com.t3ddyss.clother.data.chat.ChatListenerRepositoryImpl
import com.t3ddyss.clother.data.chat.ChatRepositoryImpl
import com.t3ddyss.clother.data.chat.NotificationControllerImpl
import com.t3ddyss.clother.domain.chat.ChatInteractor
import com.t3ddyss.clother.domain.chat.ChatInteractorImpl
import com.t3ddyss.clother.domain.chat.ChatListenerRepository
import com.t3ddyss.clother.domain.chat.ChatRepository
import com.t3ddyss.clother.domain.chat.NotificationController
import com.t3ddyss.clother.domain.chat.NotificationInteractor
import com.t3ddyss.clother.domain.chat.NotificationInteractorImpl
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
    fun bindChatInteractor(chatInteractorImpl: ChatInteractorImpl): ChatInteractor

    @Singleton
    @Binds
    fun bindChatRepository(chatRepositoryImpl: ChatRepositoryImpl): ChatRepository

    @Singleton
    @Binds
    fun bindChatListenerRepository(chatListenerRepositoryImpl: ChatListenerRepositoryImpl): ChatListenerRepository

    @Singleton
    @Binds
    fun bindNotificationInteractor(notificationInteractorImpl: NotificationInteractorImpl): NotificationInteractor

    @Singleton
    @Binds
    fun bindNotificationController(notificationControllerImpl: NotificationControllerImpl): NotificationController
}