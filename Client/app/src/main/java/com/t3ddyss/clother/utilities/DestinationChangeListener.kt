package com.t3ddyss.clother.utilities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.t3ddyss.clother.MainActivity
import com.t3ddyss.clother.R
import com.t3ddyss.clother.databinding.ActivityMainBinding

class DestinationChangeListener(
    private val binding: ActivityMainBinding,
    private val activity: MainActivity,
    private val notificationUtil: NotificationUtil
) : NavController.OnDestinationChangedListener {
    private val fragmentsWithoutBottomNav = setOf(
        R.id.emailActionFragment,
        R.id.offerEditorFragment, R.id.resetPasswordFragment, R.id.signInFragment,
        R.id.signUpFragment, R.id.galleryFragment, R.id.locationFragment,
        R.id.offerFragment, R.id.locationViewerFragment, R.id.searchFragment, R.id.chatFragment
    )

    private val fragmentsWithoutToolbar = setOf(R.id.searchFragment)

    private val fragmentsWithToolbarLabel = setOf(
        R.id.offerCategoryFragment,
        R.id.offerEditorFragment, R.id.galleryFragment, R.id.locationFragment,
        R.id.locationViewerFragment, R.id.searchByCategoryFragment, R.id.chatFragment,
        R.id.homeFragment, R.id.chatsFragment, R.id.profileFragment
    )

    private val fragmentsWithCustomUpIcon = setOf(
        R.id.offerEditorFragment,
        R.id.galleryFragment, R.id.locationFragment
    )

    private val fragmentsWithoutNavIcon = setOf(
        R.id.homeFragment,
        R.id.profileFragment, R.id.searchByCategoryFragment,
        R.id.searchFragment, R.id.signUpFragment, R.id.chatsFragment
    )

    private val fragmentsOverlayingToolbar = setOf(R.id.offerFragment)

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        notificationUtil.isChatFragment = destination.id == R.id.chatFragment
        notificationUtil.isChatsFragment = destination.id == R.id.chatsFragment

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
            binding.navHostFragmentMarginTop.isVisible =
                destination.id !in fragmentsOverlayingToolbar
                        && destination.id !in fragmentsWithoutToolbar

            // Toolbar icon
            if (destination.id !in fragmentsWithoutNavIcon
                && destination.id in fragmentsWithCustomUpIcon
            ) {
                setIconClose(toolbar)
            } else if (destination.id !in fragmentsWithoutNavIcon) {
                setIconUp(toolbar)
            }

            // Toolbar title
            if (destination.id in fragmentsWithToolbarLabel) {
                activity.supportActionBar?.setDisplayShowTitleEnabled(true)
            } else {
                activity.supportActionBar?.setDisplayShowTitleEnabled(false)
            }

            // Profile icon
            binding.cardViewAvatar.imageViewAvatar.isVisible =
                destination.id == R.id.profileFragment

        }
    }

    private fun setIconClose(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.navigationIcon?.colorFilter =
            activity.getThemeColor(R.attr.colorOnPrimary).toColorFilter()
    }

    fun setIconUp(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.navigationIcon?.colorFilter =
            activity.getThemeColor(R.attr.colorOnPrimary).toColorFilter()
    }
}