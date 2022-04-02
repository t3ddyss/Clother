package com.t3ddyss.clother.data.remote

import com.t3ddyss.clother.data.remote.dto.OfferDto
import com.t3ddyss.clother.data.remote.dto.OfferPostResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface RemoteOffersService {
    @GET("api/offers")
    suspend fun getOffers(
        @Header("Authorization") accessToken: String?,
        @Query("after") afterKey: Int? = null,
        @Query("before") beforeKey: Int? = null,
        @Query("limit") limit: Int = 10,
        @QueryMap filters: Map<String, String>? = null
    ): List<OfferDto>

    @Multipart
    @POST("api/offers/new")
    suspend fun postOffer(
        @Header("Authorization") accessToken: String?,
        @Part("request") body: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): OfferPostResponseDto

    @DELETE("api/offers/delete")
    suspend fun deleteOffer(
        @Header("Authorization") accessToken: String?,
        @Query("offer") offerId: Int
    ): Response<*>
}