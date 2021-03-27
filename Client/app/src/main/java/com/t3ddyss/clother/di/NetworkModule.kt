package com.t3ddyss.clother.di

import android.os.Build
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.api.ClotherOffersService
import com.t3ddyss.clother.api.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // TODO remove this after deployment
    private const val BASE_URL_EMULATOR = "http://10.0.2.2:5000/"
    private const val BASE_URL_DEVICE = "http://192.168.0.105:5000/"

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

//        val logging = HttpLoggingInterceptor()
//        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS)

        return clientBuilder
            .authenticator(authenticator)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(httpClient: OkHttpClient): Retrofit {
        val baseUrl = if (Build.FINGERPRINT.contains("generic"))
            BASE_URL_EMULATOR else BASE_URL_DEVICE

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
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
}