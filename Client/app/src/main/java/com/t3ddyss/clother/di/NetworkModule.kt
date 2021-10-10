package com.t3ddyss.clother.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.api.TokenAuthenticator
import com.t3ddyss.clother.models.dto.GsonDateAdapter
import com.t3ddyss.clother.utilities.baseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideHttpClient(authenticator: TokenAuthenticator): OkHttpClient {
        val clientBuilder = OkHttpClient().newBuilder()

        // Unexpected end of stream emulator issue https://github.com/square/okhttp/issues/2738
        clientBuilder.interceptors().add(Interceptor {
            it.run {
                proceed(
                    request()
                        .newBuilder()
                        .addHeader("Connection", "close")
                        .build()
                )
            }
        })

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        return clientBuilder
            .authenticator(authenticator)
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Singleton
    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = baseUrl

    @Singleton
    @Provides
    fun provideRetrofit(
        httpClient: OkHttpClient,
        @BaseUrl
        baseUrl: String,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient)
            .build()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object NetworkServiceModule {
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

        @Singleton
        @Provides
        fun provideGson(): Gson {
            return GsonBuilder()
                .registerTypeAdapter(Date::class.java, GsonDateAdapter())
                .create()
        }
    }

    annotation class BaseUrl
}