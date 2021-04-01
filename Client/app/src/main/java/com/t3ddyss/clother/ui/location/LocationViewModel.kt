package com.t3ddyss.clother.ui.location

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.t3ddyss.clother.models.LatLngWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val location = MutableLiveData(LatLngWrapper())
    var isEnablingLocationRequested = false
}