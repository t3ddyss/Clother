package com.t3ddyss.clother.api

import com.t3ddyss.clother.models.Category
import com.t3ddyss.clother.models.NewOfferResponse
import com.t3ddyss.clother.models.Offer
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ClotherOffersService {
    @GET("api/offers")
    suspend fun getOffers(@Header("Authorization") accessToken: String?,
                          @Query("after") afterKey: Int? = null,
                          @Query("before") beforeKey: Int? = null,
                          @Query("size") size: Int = 10,
                          @QueryMap filters: Map<String, String>? = null): List<Offer>

    @GET("api/offers/categories")
    suspend fun getCategories(@Header("Authorization") accessToken: String?): List<Category>

    @Multipart
    @POST("api/offers/new")
    suspend fun postOffer(@Header("Authorization") accessToken: String?,
                          @Part("request") body: RequestBody,
                          @Part files: MultipartBody.Part): NewOfferResponse
}