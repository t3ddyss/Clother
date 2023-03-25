package com.t3ddyss.clother.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.android.gms.maps.model.LatLng
import com.t3ddyss.clother.domain.offers.OffersInteractor
import com.t3ddyss.clother.domain.offers.models.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val offersInteractor: OffersInteractor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val args = SearchResultsFragmentArgs
        .fromSavedStateHandle(savedStateHandle)
    private val _offers = MutableLiveData<PagingData<Offer>>()
    val offers: LiveData<PagingData<Offer>> = _offers

    val location = MutableLiveData<Pair<LatLng, Int>?>()
    val size = MutableLiveData<String?>()
    val filters = MutableLiveData(Unit)

    private var currentQuery: Map<String, String>? = null
    var endOfPaginationReachedBottom = false

    init {
        filters.observeForever {
            onFiltersChange()
        }
    }

    private fun onFiltersChange() {
        val query = getSearchQuery()
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

    private fun getSearchQuery() = buildMap {
        args.category?.let {
            put(QUERY_PARAM_CATEGORY, it.id.toString())
        }

        args.query?.let {
            put(QUERY_PARAM_QUERY, it)
        }

        this@SearchResultsViewModel.size.value?.let {
            put(QUERY_PARAM_SIZE, it)
        }

        location.value?.let {
            put(QUERY_PARAM_LOCATION, "${it.first.latitude},${it.first.longitude}")
            put(QUERY_PARAM_RADIUS, it.second.toString())
        }
    }

    private companion object {
        const val QUERY_PARAM_CATEGORY = "category"
        const val QUERY_PARAM_QUERY = "query"
        const val QUERY_PARAM_LOCATION = "location"
        const val QUERY_PARAM_RADIUS = "radius"
        const val QUERY_PARAM_SIZE = "size"
    }
}