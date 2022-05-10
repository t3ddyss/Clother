package com.t3ddyss.clother.util

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.t3ddyss.clother.R
import com.t3ddyss.navigation.presentation.models.UserArg

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
                val user = arguments?.getParcelable<UserArg>(ARG_USER)
                changeVisibilityIfNeeded(navView, user == null)
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
        const val ARG_USER = "user"
    }
}