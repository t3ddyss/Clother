package com.t3ddyss.clother.api

import com.t3ddyss.clother.models.Offer
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ClotherOffersService {
    @GET("api/offers")
    suspend fun getOffers(@Header("Authorization") accessToken: String?,
                          @Query("after") afterKey: Int? = null,
                          @Query("before") beforeKey: Int? = null,
                          @Query("size") size: Int = 10,
                          @QueryMap filters: Map<String, String>? = null): List<Offer>
}