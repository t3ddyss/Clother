package com.t3ddyss.clother.domain.common.navigation

import javax.inject.Inject

class NavigationInteractorImpl @Inject constructor(
    private val navigationRepository: NavigationRepository
) : NavigationInteractor {
    override var interlocutorId: Int? = null

    override var destinationId: Int? = null

    override fun isScreen(screen: Screen): Boolean {
        return destinationId?.let {
            navigationRepository.isScreen(screen, it)
        } ?: false
    }

    override fun isAuthRequiredForCurrentDestination(): Boolean {
        return destinationId?.let {
            navigationRepository.isAuthRequiredForDestination(it)
        } ?: false
    }
}