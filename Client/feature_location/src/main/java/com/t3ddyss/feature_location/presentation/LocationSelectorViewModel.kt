package com.t3ddyss.feature_location.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.feature_location.domain.LocationData
import com.t3ddyss.feature_location.domain.LocationInteractor
import com.t3ddyss.feature_location.domain.LocationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class LocationSelectorViewModel
@Inject constructor(
    private val interactor: LocationInteractor
) : ViewModel() {
    private val _location = MutableStateFlow(LocationData.DEFAULT)
    val location: LiveData<LocationData> = _location.asLiveData()

    private val isLocationRequested = AtomicBoolean(false)
    val isLocationEnablingRequested = AtomicBoolean(false)

    fun requestLocation() {
        if (isLocationRequested.getAndSet(true)) return
        viewModelScope.launch {
            interactor.observeLocation().collectLatest(_location::emit)
        }
    }

    fun setLocationManually(latLng: LatLng) {
        _location.value = LocationData(
            latLng = latLng,
            locationType = LocationType.MANUALLY_SELECTED
        )
    }

    fun saveLocation() {
        viewModelScope.launch {
            interactor.saveLocationIfNeeded(_location.value)
        }
    }
}