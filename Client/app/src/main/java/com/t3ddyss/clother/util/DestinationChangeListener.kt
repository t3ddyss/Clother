package com.t3ddyss.clother.util

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.t3ddyss.clother.R

class DestinationChangeListener(
    private val navView: View
) : NavController.OnDestinationChangedListener {

    private val fragmentsWithBottomMenu = setOf(
        R.id.homeFragment,
        R.id.searchByCategoryFragment,
        R.id.offerCategoryFragment,
        R.id.chatsFragment,
    )

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.profileFragment -> {
                val isCurrentUser = arguments?.getBoolean(ARG_IS_CURRENT_USER) ?: true
                changeVisibilityIfNeeded(navView, isCurrentUser)
            }
            else -> {
                changeVisibilityIfNeeded(navView, destination.id in fragmentsWithBottomMenu)
            }
        }
    }

    private fun changeVisibilityIfNeeded(view: View, isVisible: Boolean) {
        if (view.isVisible != isVisible) {
            view.isVisible = isVisible
        }
    }

    private companion object {
        const val ARG_IS_CURRENT_USER = "is_current_user"
    }
}