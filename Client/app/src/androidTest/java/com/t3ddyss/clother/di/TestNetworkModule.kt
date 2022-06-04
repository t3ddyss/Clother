package com.t3ddyss.clother.di

import com.google.gson.Gson
import com.t3ddyss.clother.di.common.common.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
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
    fun provideHttpClient(): OkHttpClient = OkHttpClient().newBuilder().build()

    @Singleton
    @Provides
    fun provideMockWebServer(): MockWebServer = MockWebServer()

    @Singleton
    @Provides
    fun provideRetrofit(
        httpClient: OkHttpClient,
        mockWebServer: MockWebServer,
        gson: Gson
    ): Retrofit {
        // Implicitly starts MockWebServer
        return Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()
    }

    @Singleton
    @Provides
    @NetworkModule.BaseUrl
    fun provideBaseUrl(): String = "http://10.0.2.2:5000/"
}