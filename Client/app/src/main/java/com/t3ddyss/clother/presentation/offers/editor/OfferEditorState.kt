package com.t3ddyss.clother.presentation.offers.editor

import com.t3ddyss.clother.domain.offers.OffersInteractor

sealed interface OfferEditorState {
    object Loading : OfferEditorState
    data class ValidationError(val causes: List<OffersInteractor.OfferParam>) : OfferEditorState
    object Success : OfferEditorState
    object Error : OfferEditorState
}