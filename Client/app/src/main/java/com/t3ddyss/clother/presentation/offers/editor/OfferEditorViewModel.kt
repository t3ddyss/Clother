package com.t3ddyss.clother.presentation.offers.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.util.Event
import com.t3ddyss.clother.util.toEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfferEditorViewModel @Inject constructor(
    private val offersInteractor: OffersInteractor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = OfferEditorFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _state = MutableSharedFlow<OfferEditorState>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val state = _state.asSharedFlow().distinctUntilChanged()

    private val _error = MutableSharedFlow<Event<OfferEditorError>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val error = _error.asSharedFlow()

    private val _images = MutableStateFlow(emptyList<Uri>())
    val images = _images.asStateFlow()

    private val _location = MutableStateFlow<LatLng?>(null)
    val location = _location.asStateFlow()

    fun selectImages(images: List<Uri>) {
        _images.value = images
    }

    fun selectLocation(location: LatLng) {
        _location.value = location
    }

    fun postOffer(
        title: String,
        description: String,
        size: String?
    ) {
        viewModelScope.launch {
            offersInteractor.validateParameters(title, images.value)
                .tap {
                    _state.emit(OfferEditorState.Loading)
                    offersInteractor.postOffer(
                        title = title,
                        categoryId = args.category.id,
                        description = description,
                        images = images.value,
                        size = size,
                        location = location.value
                    )
                        .tap {
                            _state.emit(OfferEditorState.Success)
                        }
                        .tapLeft {
                            _state.emit(OfferEditorState.Error)
                            _error.emit(OfferEditorError.Network(it).toEvent())
                        }
                }
                .tapLeft { errors ->
                    _state.emit(OfferEditorState.ValidationError(errors))
                    if (errors.contains(OffersInteractor.OfferParam.IMAGES)) {
                        _error.emit(OfferEditorError.NoImagesSelected.toEvent())
                    }
                }
        }
    }
}