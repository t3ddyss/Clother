package com.t3ddyss.clother.presentation.search

import android.view.View
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
    val location = MutableLiveData<LatLng>()
    val maxDistance = MutableLiveData(View.NO_ID)
    val size = MutableLiveData(View.NO_ID)

    init {
        viewModelScope.launch {
            locationInteractor.getLastSavedLocationOrNull()?.let {
                location.postValue(it.latLng)
            }
        }
    }
}