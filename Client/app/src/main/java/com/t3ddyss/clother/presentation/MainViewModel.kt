package com.t3ddyss.clother.presentation

import androidx.lifecycle.*
import com.t3ddyss.clother.domain.ConnectivityObserver
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.models.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
    authInteractor: AuthInteractor
) : ViewModel() {
    private val _networkAvailability = MutableLiveData<Boolean>()
    val networkAvailability: LiveData<Boolean> = _networkAvailability

    val authStateFlow: StateFlow<AuthState> = authInteractor.authState
    val unauthorizedEvent =
        authStateFlow
            .filter { it is AuthState.None }
            .asLiveData()

    private var isNetworkPreviouslyAvailable: Boolean? = null

    init {
        viewModelScope.launch {
            connectivityObserver.observeConnectivityStatus().collect { isNetworkAvailable ->
                if (isNetworkPreviouslyAvailable == null && !isNetworkAvailable) {
                    _networkAvailability.postValue(isNetworkAvailable)
                } else if (isNetworkPreviouslyAvailable != null
                    && isNetworkPreviouslyAvailable != isNetworkAvailable
                ) {
                    _networkAvailability.postValue(isNetworkAvailable)
                }

                isNetworkPreviouslyAvailable = isNetworkAvailable
            }
        }
    }
}