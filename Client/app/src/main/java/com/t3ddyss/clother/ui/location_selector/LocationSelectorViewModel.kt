package com.t3ddyss.clother.ui.location_selector

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.data.LocationProvider
import com.t3ddyss.clother.models.domain.LocationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationSelectorViewModel
@Inject constructor(
    private val repository: LocationProvider
) : ViewModel() {
    private val _location = MutableLiveData(LocationData())
    val location: LiveData<LocationData> = _location

    var isEnablingLocationRequested = false
    private var isLocationRequested = false

    fun getLocation() {
        if (isLocationRequested) return
        viewModelScope.launch {
            isLocationRequested = true
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