package com.t3ddyss.clother.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.api.TokenAuthenticator
import com.t3ddyss.clother.utilities.getBaseUrlForCurrentDevice
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideHttpClient(authenticator: TokenAuthenticator): OkHttpClient {
        val clientBuilder = OkHttpClient().newBuilder()

        // Unexpected end of stream issue https://github.com/square/okhttp/issues/2738
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
    fun provideGson(): Gson {
        return GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create()
    }

    @Singleton
    @Provides
    fun provideRetrofit(httpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getBaseUrlForCurrentDevice())
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