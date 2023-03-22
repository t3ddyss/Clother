package com.t3ddyss.clother.presentation.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.filter
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.ProfileInteractor
import com.t3ddyss.clother.domain.auth.models.UserInfoState
import com.t3ddyss.clother.presentation.offers.DeletedOffersHolder
import com.t3ddyss.clother.util.toEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    profileInteractor: ProfileInteractor,
    authInteractor: AuthInteractor,
    savedStateHandle: SavedStateHandle,
    private val deletedOffersHolder: DeletedOffersHolder
) : ViewModel() {
    private val args = ProfileFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val userId = args.user?.id ?: authInteractor.authStateFlow.value.userId

    val user = profileInteractor
        .observeUserInfo(userId)
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val offers = profileInteractor
        .observeOffersByUser(userId)
        .cachedIn(viewModelScope)
        .combine(deletedOffersHolder.offers) { offers, deleted ->
            offers.filter { it.id !in deleted }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    val error = user.filterIsInstance<UserInfoState.Error>()
        .map { it.error.toEvent() }
        .shareIn(viewModelScope, SharingStarted.Lazily, 1)
}