package com.t3ddyss.clother.ui.filters

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.data.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val repository: LocationProvider
) : ViewModel() {
    val location = MutableLiveData<LatLng>()
    val maxDistance = MutableLiveData(View.NO_ID)
    val size = MutableLiveData(View.NO_ID)
    private val isSavedLocationRetrieved = AtomicBoolean(false)

    fun getSavedLocation() {
        if (isSavedLocationRetrieved.getAndSet(true)) return

        viewModelScope.launch {
            repository.getLatestSavedLocation()?.let { loc ->
                location.postValue(LatLng(loc.lat, loc.lng))
            }
        }
    }
}