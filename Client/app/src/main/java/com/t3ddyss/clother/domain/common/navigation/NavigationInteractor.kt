package com.t3ddyss.clother.domain.common.navigation

interface NavigationInteractor {
    var interlocutorId: Int?
    var destinationId: Int?
    fun isScreen(screen: Screen): Boolean
    fun isAuthRequiredForCurrentDestination(): Boolean
}