package com.t3ddyss.clother.ui.location_selector

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.t3ddyss.clother.models.LatLngWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationSelectorViewModel @Inject constructor(
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val location = MutableLiveData(LatLngWrapper())
    var isEnablingLocationRequested = false
}