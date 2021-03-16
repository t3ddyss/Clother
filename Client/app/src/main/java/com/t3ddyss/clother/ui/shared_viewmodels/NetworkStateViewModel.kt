package com.t3ddyss.clother.ui.shared_viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NetworkStateViewModel @Inject constructor(): ViewModel() {
    // Pair.second is true if network is available,
    // Pair.first is true if Pair.second is meaningful for us
    val isNetworkAvailable = MutableLiveData<Pair<Boolean, Boolean>>()
}