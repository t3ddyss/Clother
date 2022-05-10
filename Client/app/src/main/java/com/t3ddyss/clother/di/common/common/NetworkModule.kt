package com.t3ddyss.clother.di.common.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.t3ddyss.clother.data.auth.remote.RemoteAuthService
import com.t3ddyss.clother.data.chat.remote.RemoteChatService
import com.t3ddyss.clother.data.common.common.remote.GsonDateAdapter
import com.t3ddyss.clother.data.offers.remote.RemoteOffersService
import com.t3ddyss.core.util.utils.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
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

        val logging = HttpLoggingInterceptor()
        val loggingLevel = if (Utils.isDebug) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        logging.level = loggingLevel

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
        "http://192.168.0.103:5000/"
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