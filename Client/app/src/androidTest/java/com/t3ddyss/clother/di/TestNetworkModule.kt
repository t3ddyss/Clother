package com.t3ddyss.clother.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.api.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class])
object TestNetworkModule {

    @Singleton
    @Provides
    fun provideHttpClient(authenticator: TokenAuthenticator): OkHttpClient {
        val clientBuilder = OkHttpClient().newBuilder()

//        clientBuilder.interceptors().add(Interceptor {
//            it.run {
//                proceed(
//                    request()
//                        .newBuilder()
//                        .addHeader("Connection", "close")
//                        .build()
//                )
//            }
//        })

        return clientBuilder
            .authenticator(authenticator)
//            .retryOnConnectionFailure(true)
            .build()
    }

    @Singleton
    @Provides
    fun provideMockWebServer(): MockWebServer = MockWebServer()

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create()
    }

    @Singleton
    @Provides
    fun provideRetrofit(httpClient: OkHttpClient, mockWebServer: MockWebServer, gson: Gson):
            Retrofit {
        // Implicitly starts MockWebServer
        return Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideClotherAuthService(retrofit: Retrofit): ClotherAuthService =
        retrofit.create(ClotherAuthService::class.java)

    @Singleton
    @Provides
    fun provideClotherOffersService(retrofit: Retrofit): ClotherOffersService =
        retrofit.create(ClotherOffersService::class.java)

    @Singleton
    @Provides
    fun provideClotherChatService(retrofit: Retrofit): ClotherChatService =
        retrofit.create(ClotherChatService::class.java)
}