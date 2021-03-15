package com.t3ddyss.clother.api

import com.t3ddyss.clother.models.Offer
import com.t3ddyss.clother.models.OfferResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ClotherOffersService {
    @GET("offers")
    suspend fun getOffers(@Query("after") afterKey: Int? = null,
                          @Query("before") beforeKey: Int? = null,
                          @Query("size") size: Int = 10,
                          @QueryMap filters: Map<String, String>? = null): List<Offer>
}