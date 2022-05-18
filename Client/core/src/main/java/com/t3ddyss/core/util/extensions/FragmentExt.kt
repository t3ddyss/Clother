package com.t3ddyss.core.util.extensions

import android.util.TypedValue
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.t3ddyss.core.R
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.InfoMessage
import com.t3ddyss.core.presentation.NavMenuController
import com.t3ddyss.core.util.utils.StringUtils
import kotlin.math.roundToInt

val Fragment.dp: Int.() -> Int
    get() = {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            this@dp.requireContext().resources.displayMetrics
        ).roundToInt()
    }

val Fragment.errorText: Error<*>.() -> String
    get() = {
        when (message) {
            is InfoMessage.StringMessage -> {
                message.message ?: StringUtils.getErrorText(throwable, requireContext())
            }
            is InfoMessage.StringResMessage -> {
                getString(message.messageRes)
            }
            else -> {
                StringUtils.getErrorText(throwable, requireContext())
            }
        }
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

fun Fragment.showSnackbarWithText(error: Error<*>) {
    showSnackbarWithText(error.errorText())
}

fun Fragment.showSnackbarWithText(throwable: Throwable) {
    showSnackbarWithText(StringUtils.getErrorText(throwable, requireContext()))
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