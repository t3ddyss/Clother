package com.t3ddyss.clother.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.t3ddyss.clother.data.OffersRepository
import com.t3ddyss.clother.models.domain.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject constructor(
    private val offersRepository: OffersRepository
) : ViewModel() {
    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    private val isOffersLoaded = AtomicBoolean(false)

    @ExperimentalPagingApi
    fun getOffers(userId: Int) {
        if (isOffersLoaded.getAndSet(true)) return

        viewModelScope.launch {
            offersRepository
                .getOffers(query = mapOf("user" to userId.toString()), userId = userId)
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