package com.t3ddyss.clother.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.utilities.ConnectivityUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkStateViewModel @Inject constructor(
    private val connectivityUtil: ConnectivityUtil
) : ViewModel() {
    private val _networkAvailability = MutableLiveData<Boolean>()
    val networkAvailability: LiveData<Boolean> = _networkAvailability

    private var isNetworkPreviouslyAvailable: Boolean? = null

    init {
        viewModelScope.launch {
            connectivityUtil.getConnectivityStatusStream().collect { isNetworkAvailable ->
                if (isNetworkPreviouslyAvailable == null && !isNetworkAvailable) {
                    _networkAvailability.postValue(isNetworkAvailable)
                }

                else if (isNetworkPreviouslyAvailable != null
                    && isNetworkPreviouslyAvailable != isNetworkAvailable) {
                    _networkAvailability.postValue(isNetworkAvailable)
                }

                isNetworkPreviouslyAvailable = isNetworkAvailable
            }
        }
    }
}