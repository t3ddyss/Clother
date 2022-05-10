package com.t3ddyss.core.util.utils

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.t3ddyss.core.R

object ToolbarUtils {
    fun setupToolbar(
        activity: Activity?,
        toolbar: Toolbar,
        title: String = "",
        navIcon: NavIcon = NavIcon.NONE
    ) {
        (activity as? AppCompatActivity)?.apply {
            toolbar.title = title
            navIcon.iconRes?.let {
                toolbar.setNavigationIcon(it)
            }
            setSupportActionBar(toolbar)
        }
    }

    enum class NavIcon(@DrawableRes val iconRes: Int?) {
        NONE(null),
        UP(R.drawable.ic_arrow_back),
        CLOSE(R.drawable.ic_close)
    }
}