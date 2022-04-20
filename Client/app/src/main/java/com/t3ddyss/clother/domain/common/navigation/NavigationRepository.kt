package com.t3ddyss.clother.domain.common.navigation

interface NavigationRepository {
    fun isScreen(screen: Screen, destinationId: Int): Boolean
}