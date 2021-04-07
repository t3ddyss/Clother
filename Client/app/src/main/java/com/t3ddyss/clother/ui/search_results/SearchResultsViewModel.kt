package com.t3ddyss.clother.ui.search_results

import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import com.t3ddyss.clother.data.OffersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class SearchResultsViewModel
@Inject constructor(
    private val repository: OffersRepository
) : ViewModel() {
    // TODO: Implement the ViewModel
}