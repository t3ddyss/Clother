package com.t3ddyss.clother.presentation.search

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.domain.models.Offer
import com.t3ddyss.clother.domain.offer.OffersInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val offersInteractor: OffersInteractor
) : ViewModel() {
    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    // Location and maximum distance
    val location = MutableLiveData<Pair<LatLng, Int>>()
    val size = MutableLiveData<String>()
    val filters = MediatorLiveData<String>().apply { value = "initial" }

    init {
        filters.addSource(location) { filters.value = "location" }
        filters.addSource(size) { filters.value = "size" }
    }

    private var currentQuery: Map<String, String>? = null
    var endOfPaginationReachedBottom = false

    fun getOffers(query: Map<String, String>) {
        if (query == currentQuery) {
            return
        }
        currentQuery = query

        viewModelScope.launch {
            offersInteractor
                .observeOffersFromNetwork(query)
                .cachedIn(viewModelScope)
                .collectLatest {
                    _offers.postValue(it)
                }
        }
    }
}