package com.t3ddyss.clother.presentation.offers.editor

import com.t3ddyss.core.domain.models.ApiCallError

sealed interface OfferEditorError {
    data class Network(val cause: ApiCallError) : OfferEditorError
    object NoImagesSelected : OfferEditorError
}