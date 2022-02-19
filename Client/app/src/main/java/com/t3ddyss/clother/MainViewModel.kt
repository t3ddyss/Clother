package com.t3ddyss.clother

import androidx.lifecycle.*
import com.t3ddyss.clother.data.UsersRepository
import com.t3ddyss.clother.models.domain.AuthState
import com.t3ddyss.clother.util.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
    usersRepository: UsersRepository
) : ViewModel() {
    private val _networkAvailability = MutableLiveData<Boolean>()
    val networkAvailability: LiveData<Boolean> = _networkAvailability

    val authStateFlow: StateFlow<AuthState> = usersRepository.observeAuthState()
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