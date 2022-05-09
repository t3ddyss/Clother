package com.t3ddyss.clother.util

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ActivityMainBinding

// TODO use individual toolbar in every fragment
class DestinationChangeListener(
    private val binding: ActivityMainBinding,
    private val actionBarProvider: () -> ActionBar?
) : NavController.OnDestinationChangedListener {

    private val context get() = binding.root.context

    private val fragmentsWithoutBottomNav = setOf(
        R.id.emailActionFragment,
        R.id.offerEditorFragment, R.id.resetPasswordFragment, R.id.signInFragment,
        R.id.signUpFragment, R.id.galleryFragment, R.id.locationFragment,
        R.id.offerFragment, R.id.locationViewerFragment, R.id.searchFragment, R.id.chatFragment,
        R.id.imageSelectorDialog, R.id.imageFragment, R.id.messageMenuDialog, R.id.onboardingFragment
    )

    private val fragmentsWithoutToolbar = setOf(R.id.searchFragment, R.id.onboardingFragment, R.id.profileFragment)

    private val fragmentsWithToolbarLabel = setOf(
        R.id.offerFragment,
        R.id.offerCategoryFragment,
        R.id.offerEditorFragment, R.id.galleryFragment, R.id.locationFragment,
        R.id.locationViewerFragment, R.id.searchByCategoryFragment, R.id.chatFragment,
        R.id.homeFragment, R.id.chatsFragment, R.id.profileFragment, R.id.imageSelectorDialog,
        R.id.messageMenuDialog
    )

    private val fragmentsWithoutNavIcon = setOf(
        R.id.homeFragment,
        R.id.profileFragment, R.id.searchByCategoryFragment, R.id.offerCategoryFragment,
        R.id.searchFragment, R.id.signUpFragment, R.id.chatsFragment
    )

    private val fragmentsWithCloseIcon = setOf(
        R.id.offerEditorFragment,
        R.id.galleryFragment, R.id.locationFragment
    )

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {

        with(binding) {
            // NavView visibility
            if (destination.id !in fragmentsWithoutBottomNav && !navView.isVisible) {
                navView.isVisible = true
            } else if (destination.id in fragmentsWithoutBottomNav && navView.isVisible) {
                navView.isVisible = false
            }

            // Toolbar visibility
            if (destination.id !in fragmentsWithoutToolbar && !toolbar.isVisible) {
                toolbar.isVisible = true
            } else if (destination.id in fragmentsWithoutToolbar && toolbar.isVisible) {
                toolbar.isVisible = false
            }

            // Toolbar icon
            when (destination.id) {
                R.id.searchByCategoryFragment, R.id.offerCategoryFragment -> {
                    val isRoot = arguments?.getInt("parent_id")?.let { it == 0 } ?: true
                    if (isRoot) {
                        toolbar.navigationIcon = null
                    }
                    else {
                        setIconUp(toolbar)
                    }
                }
                R.id.profileFragment -> {
                    val isCurrentUser = arguments?.getBoolean("is_current_user", true) ?: true
                }
                in fragmentsWithCloseIcon -> {
                    setIconClose(toolbar)
                }
                !in fragmentsWithoutNavIcon -> {
                    setIconUp(toolbar)
                }
                else -> {
                    toolbar.navigationIcon = null
                }
            }

            // Toolbar title
            if (destination.id in fragmentsWithToolbarLabel) {
                actionBarProvider()?.setDisplayShowTitleEnabled(true)
            } else {
                actionBarProvider()?.setDisplayShowTitleEnabled(false)
            }
        }
    }

    private fun setIconClose(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.navigationIcon?.colorFilter = context
            .getThemeColor(R.attr.colorOnPrimary)
            .toColorFilter()
    }

    private fun setIconUp(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.navigationIcon?.colorFilter = context
            .getThemeColor(R.attr.colorOnPrimary)
            .toColorFilter()
    }
}