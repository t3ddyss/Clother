package com.t3ddyss.core.util.extensions

import android.util.TypedValue
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.core.R
import com.t3ddyss.core.presentation.NavMenuController
import kotlin.math.roundToInt

context(Fragment)
val Int.dp: Int
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(),
            requireContext().resources.displayMetrics
        ).roundToInt()
    }

fun Fragment.showSnackbarWithText(text: String?) {
    val snackbar = Snackbar.make(
        requireView(),
        text ?: getString(R.string.error_unknown),
        Snackbar.LENGTH_SHORT
    )
    showSnackbar(snackbar)
}

fun Fragment.showSnackbarWithText(@StringRes textRes: Int?) {
    val text = textRes?.let {
        getString(it)
    }
    showSnackbarWithText(text)
}

fun Fragment.showSnackbarWithAction(
    @StringRes text: Int,
    @StringRes actionText: Int,
    action: (() -> Unit)
) {
    val snackbar = Snackbar.make(
        requireView(),
        getString(text),
        Snackbar.LENGTH_SHORT
    )
    snackbar.setAction(actionText) {
        action.invoke()
    }
    showSnackbar(snackbar)
}

private fun Fragment.showSnackbar(snackbar: Snackbar) {
    val navMenuController = activity as? NavMenuController
    if (navMenuController?.isMenuVisible == true) {
        snackbar.anchorView = navMenuController.menuView
    }
    snackbar.show()
}