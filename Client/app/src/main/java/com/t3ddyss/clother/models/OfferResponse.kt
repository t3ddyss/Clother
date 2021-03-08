package com.t3ddyss.clother.models

import com.google.gson.annotations.SerializedName

data class OfferResponse(val results: List<Offer>,
                         @SerializedName("total_pages") val totalPages: Int)
