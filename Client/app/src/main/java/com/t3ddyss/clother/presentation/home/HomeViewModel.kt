package com.t3ddyss.clother.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.t3ddyss.clother.domain.offers.OffersInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    offersInteractor: OffersInteractor
) : ViewModel() {

    val offers = offersInteractor
        .observeOffersFromDatabase()
        .cachedIn(viewModelScope)

    var endOfPaginationReachedBottom = false
}