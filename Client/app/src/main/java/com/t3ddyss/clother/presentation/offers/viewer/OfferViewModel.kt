package com.t3ddyss.clother.presentation.offers.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.presentation.offers.DeletedOffersHolder
import com.t3ddyss.clother.util.toEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

@HiltViewModel
class OfferViewModel @Inject constructor(
    private val offersInteractor: OffersInteractor,
    private val authInteractor: AuthInteractor,
    private val deletedOffersHolder: DeletedOffersHolder,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val args = OfferFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _state = MutableSharedFlow<OfferState>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val state = _state.asSharedFlow().distinctUntilChanged()

    val error = state.filterIsInstance<OfferState.DeletionError>()
        .map { it.toEvent() }
        .shareIn(viewModelScope, SharingStarted.Lazily, 1)

    val isOtherUser get() = authInteractor.authStateFlow.value.userId != args.offer.user.id

    init {
        viewModelScope.launch {
            _state.emit(OfferState.Initial(offersInteractor.getOffer(args.offer.id)))
        }
    }

    fun deleteOffer() {
        viewModelScope.launch {
            _state.emit(OfferState.Loading(state.first().offer))
            offersInteractor.deleteOffer(args.offer.id)
                .tap {
                    deletedOffersHolder.onOfferDeleted(args.offer.id)
                    _state.emit(OfferState.DeletionSuccess(state.first().offer))
                }
                .tapLeft {
                    _state.emit(OfferState.DeletionError(state.first().offer, it))
                }
        }
    }
}