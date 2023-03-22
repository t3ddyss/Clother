package com.t3ddyss.clother.presentation.offers.viewer

import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.domain.models.ApiCallError

sealed interface OfferState {
    val offer: Offer

    data class Initial(override val offer: Offer) : OfferState
    data class Loading(override val offer: Offer) : OfferState
    data class DeletionSuccess(override val offer: Offer) : OfferState
    data class DeletionError(override val offer: Offer, val error: ApiCallError) : OfferState
}