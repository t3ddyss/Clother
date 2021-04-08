package com.t3ddyss.clother.ui.location_selector

import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.data.LocationProvider
import com.t3ddyss.clother.models.LatLngWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class LocationSelectorViewModel
@Inject constructor(
        private val repository: LocationProvider,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _location = MutableLiveData(LatLngWrapper())
    val location: LiveData<LatLngWrapper> = _location

    var isEnablingLocationRequested = false
    private var isLocationRequested = false

    fun getLocation() {
        if (isLocationRequested) return
        viewModelScope.launch {
            isLocationRequested = true
            repository.getLocation().collectLatest {
                _location.postValue(it)
            }
        }
    }

    fun setLocationManually(latLng: LatLng) {
        _location.value = LatLngWrapper(
                latLng = latLng,
                isInitialValue = false,
                isManuallySelected = true,
        )
    }
}