package com.t3ddyss.feature_location.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.feature_location.data.LocationProvider
import com.t3ddyss.feature_location.domain.LocationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class LocationSelectorViewModel
@Inject constructor(
    private val repository: LocationProvider
) : ViewModel() {
    private val _location = MutableLiveData(LocationData())
    val location: LiveData<LocationData> = _location

    var isEnablingLocationRequested = false
    private var isLocationRequested = AtomicBoolean(false)

    fun getLocation() {
        if (isLocationRequested.getAndSet(true)) return
        viewModelScope.launch {
            repository.observeLocation().collectLatest {
                _location.postValue(it)
            }
        }
    }

    fun setLocationManually(latLng: LatLng) {
        _location.value = LocationData(
            latLng = latLng,
            isInitialValue = false,
            isManuallySelected = true,
        )
    }

    fun saveSelectedLocation(latLng: LocationData) {
        viewModelScope.launch {
            repository.saveSelectedLocation(latLng)
        }
    }
}