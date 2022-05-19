package com.t3ddyss.clother.presentation.search

import android.view.View
import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.feature_location.domain.LocationInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val locationInteractor: LocationInteractor
) : ViewModel() {
    private val _location = MutableLiveData<LatLng?>(null)
    val location: LiveData<LatLng?> = _location

    private val _radius = MutableLiveData<Int?>(null)
    val radius: LiveData<Int?> = _radius

    private val _size = MutableLiveData<Int?>(null)
    val size: LiveData<Int?> = _size

    init {
        viewModelScope.launch {
            locationInteractor.getLastSavedLocationOrNull()?.let {
                _location.postValue(it.latLng)
            }
        }
    }

    fun onLocationSelected(coordinates: String) {
        val (lat, lng) = coordinates.split(",").map { it.toDouble() }
        _location.value = LatLng(lat, lng)
    }

    fun onRadiusSelected(@IdRes chipId: Int?) {
        _radius.value = getChipIdOrNull(chipId)
    }

    fun onSizeSelected(@IdRes chipId: Int?) {
        _size.value = getChipIdOrNull(chipId)
    }

    private fun getChipIdOrNull(@IdRes chipId: Int?): Int? {
        return if (chipId == View.NO_ID) {
            null
        } else {
            chipId
        }
    }
}