package com.t3ddyss.clother.di.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.t3ddyss.clother.data.remote.RemoteAuthService
import com.t3ddyss.clother.data.remote.RemoteChatService
import com.t3ddyss.clother.data.remote.RemoteOffersService
import com.t3ddyss.clother.data.remote.dto.GsonDateAdapter
import com.t3ddyss.core.util.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
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
    fun provideHttpClient(authenticator: Authenticator): OkHttpClient {
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
    fun provideBaseUrl(): String = if (Utils.isEmulator) {
        "http://10.0.2.2:5000/"
    } else {
        "http://192.168.0.104:5000/"
    }

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
        fun provideRemoteAuthService(retrofit: Retrofit): RemoteAuthService =
            retrofit.create(RemoteAuthService::class.java)

        @Singleton
        @Provides
        fun provideRemoteOffersService(retrofit: Retrofit): RemoteOffersService =
            retrofit.create(RemoteOffersService::class.java)

        @Singleton
        @Provides
        fun provideRemoteChatService(retrofit: Retrofit): RemoteChatService =
            retrofit.create(RemoteChatService::class.java)

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