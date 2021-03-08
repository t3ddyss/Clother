package com.t3ddyss.clother.api

import com.t3ddyss.clother.models.OfferResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ClotherOffersService {
    @GET("offers")
    suspend fun getOffers(@Query("page") page: Int = 1,
                          @Query("per_page") size: Int = 10,
                          @QueryMap filters: Map<String, String>? = null): OfferResponse
}