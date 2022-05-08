package com.t3ddyss.clother.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDestination
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.models.AuthState
import com.t3ddyss.clother.domain.common.navigation.NavigationInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val navigationInteractor: NavigationInteractor,
    authInteractor: AuthInteractor,
    storage: Storage
) : ViewModel() {
    val authStateFlow: StateFlow<AuthState> = authInteractor.authStateFlow
    val unauthorizedEvent =
        authStateFlow
            .filterIsInstance<AuthState.None>()
            .filter { navigationInteractor.isAuthRequiredForCurrentDestination() }
            .asLiveData()
    val isOnboardingCompleted = storage.isOnboardingCompleted

    fun onDestinationChange(destination: NavDestination) {
        navigationInteractor.destinationId = destination.id
    }
}