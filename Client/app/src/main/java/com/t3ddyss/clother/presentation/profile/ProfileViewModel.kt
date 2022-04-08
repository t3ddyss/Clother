package com.t3ddyss.clother.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.models.AuthState
import com.t3ddyss.clother.domain.models.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject constructor(
    private val offersRepository: OffersRepository,
    authInteractor: AuthInteractor
) : ViewModel() {
    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    init {
        val authState = authInteractor.authState.value as AuthState.Authenticated
        val userId = authState.authData.user.id

        viewModelScope.launch {
            offersRepository
                .observeOffers(query = mapOf("user" to userId.toString()), userId = userId)
                .cachedIn(viewModelScope)
                .collectLatest {
                    _offers.postValue(it)
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