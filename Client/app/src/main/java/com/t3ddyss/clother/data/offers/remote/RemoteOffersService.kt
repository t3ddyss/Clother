package com.t3ddyss.clother.data.offers.remote

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import com.t3ddyss.clother.data.offers.remote.models.OfferDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface RemoteOffersService {

    @GET("api/offers")
    suspend fun getOffers(
        @Header("Authorization") accessToken: String?,
        @Query("after") afterKey: Int?,
        @Query("before") beforeKey: Int?,
        @Query("limit") limit: Int,
        @QueryMap filters: Map<String, String>
    ): Either<CallError, List<OfferDto>>

    @Multipart
    @POST("api/offers/new")
    suspend fun postOffer(
        @Header("Authorization") accessToken: String?,
        @Part("request") body: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Either<CallError, OfferDto>

    @DELETE("api/offers/delete")
    suspend fun deleteOffer(
        @Header("Authorization") accessToken: String?,
        @Query("offer") offerId: Int
    ): Either<CallError, Unit>
}