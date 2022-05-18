package com.t3ddyss.clother.presentation.profile

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.ProfileInteractor
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.util.log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileInteractor: ProfileInteractor,
    authInteractor: AuthInteractor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val args = ProfileFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    private val _user = MutableLiveData<Resource<User>>()
    val user: LiveData<Resource<User>> = _user

    init {
        val userId = args.user?.id ?: authInteractor.authStateFlow.value.userId ?: 0
        viewModelScope.launch {
            launch {
                profileInteractor
                    .observeOffersByUser(userId)
                    .cachedIn(viewModelScope)
                    .collectLatest {
                        _offers.postValue(it)
                    }
            }
            launch {
                profileInteractor
                    .observeUserInfo(userId)
                    .catch { log("ProfileViewModel.init(): $it") } // TODO display error
                    .collectLatest {
                        _user.postValue(it)
                    }
            }
        }
    }

    fun removeOffers(removedOffers: Set<Int>) {
        val currentOffers = offers.value

        viewModelScope.launch {
            currentOffers?.filter {
                it.id !in removedOffers
            }?.let {
                _offers.postValue(it)
            }
        }
    }
}