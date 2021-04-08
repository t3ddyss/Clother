package com.t3ddyss.clother.ui.filters

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FiltersViewModel @Inject constructor() : ViewModel() {
    val location = MutableLiveData<LatLng>()
    val maxDistance = MutableLiveData(View.NO_ID)
    val size = MutableLiveData(View.NO_ID)
}