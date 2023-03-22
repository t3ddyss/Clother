package com.t3ddyss.clother.presentation.offers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

// Kludge for Paging library since it doesn't support granular changes
// https://issuetracker.google.com/issues/160232968
@Singleton
class DeletedOffersHolder @Inject constructor() {
    private val _offers = MutableStateFlow(emptySet<Int>())
    val offers = _offers.asStateFlow()

    fun onOfferDeleted(id: Int) {
        _offers.update { it + id }
    }
}