package com.t3ddyss.clother.data.common.navigation

import com.t3ddyss.clother.R
import com.t3ddyss.clother.domain.common.navigation.NavigationRepository
import com.t3ddyss.clother.domain.common.navigation.Screen
import javax.inject.Inject

class NavigationRepositoryImpl @Inject constructor() : NavigationRepository {

    override fun isScreen(screen: Screen, destinationId: Int): Boolean {
        return screen.toDestinationId() == destinationId
    }

    private fun Screen.toDestinationId(): Int {
        return when (this) {
            Screen.CHAT -> R.id.chatFragment
            Screen.CHATS -> R.id.chatsFragment
        }
    }
}