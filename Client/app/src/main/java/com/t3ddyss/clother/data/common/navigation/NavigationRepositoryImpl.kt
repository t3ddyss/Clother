package com.t3ddyss.clother.data.common.navigation

import com.t3ddyss.clother.R
import com.t3ddyss.clother.domain.common.navigation.NavigationRepository
import com.t3ddyss.clother.domain.common.navigation.Screen
import javax.inject.Inject

class NavigationRepositoryImpl @Inject constructor() : NavigationRepository {

    private val preAuthDestinations = setOf(
        R.id.onboardingFragment, R.id.signInFragment, R.id.signUpFragment,
        R.id.resetPasswordFragment, R.id.emailActionFragment
    )

    override fun isScreen(screen: Screen, destinationId: Int): Boolean {
        return screen.toDestinationId() == destinationId
    }

    override fun isAuthRequiredForDestination(destinationId: Int): Boolean {
        return destinationId !in preAuthDestinations
    }

    private fun Screen.toDestinationId(): Int {
        return when (this) {
            Screen.CHAT -> R.id.chatFragment
            Screen.CHATS -> R.id.chatsFragment
        }
    }
}