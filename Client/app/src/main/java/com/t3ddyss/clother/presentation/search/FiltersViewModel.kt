package com.t3ddyss.clother.presentation.search

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.feature_location.data.LocationRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val repository: LocationRepositoryImpl
) : ViewModel() {
    val location = MutableLiveData<LatLng>()
    val maxDistance = MutableLiveData(View.NO_ID)
    val size = MutableLiveData(View.NO_ID)

    init {
        viewModelScope.launch {
            repository.getLastLocation()?.let { loc ->
                location.postValue(loc.latLng)
            }
        }
    }
}