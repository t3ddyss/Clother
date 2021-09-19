package com.t3ddyss.clother.utilities

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.util.TypedValue
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.AttrRes
import androidx.core.animation.doOnEnd
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.util.PatternsCompat
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

private val name_regex = """\p{L}{2,50}""".toRegex()
private val password_regex =
    """^(?=.*?[0-9])(?=.*?[a-z])(?=.*?[A-Z])(?=\S+$)(?=.*?[^A-Za-z\s0-9]).{8,25}""".toRegex()

fun String.validateName() = this.matches(name_regex)

fun String.validateEmail() = PatternsCompat.EMAIL_ADDRESS.matcher(this).matches()

fun String.validatePassword() = this.matches(password_regex)

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun TextInputEditText.text() = this.text.toString().trim()

fun Context.getThemeColor(@AttrRes res: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(res, typedValue, true)
    return typedValue.data
}

fun Context.convertDpToPx(dp: Int): Int {
    val scale = resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}

fun Int.toColorFilter() = BlendModeColorFilterCompat
    .createBlendModeColorFilterCompat(this, BlendModeCompat.SRC_ATOP)

fun LatLng.toCoordinatesString(): String {
    val latitude = convertToDms(this.latitude)
    val latitudeCardinal = if (this.latitude >= 0) "N" else "S"

    val longitude = convertToDms(this.longitude)
    val longitudeCardinal = if (this.longitude >= 0) "E" else "W"
    return "$latitude$latitudeCardinal $longitude$longitudeCardinal"
}

fun Date.formatDate(): String {
    val format = SimpleDateFormat("MMM d, hh:mm a", Locale.ENGLISH)
    return format.format(this)
        .replace("AM", "am")
        .replace("PM", "pm")
}

fun Date.formatTime(): String {
    val format = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    return format.format(this)
        .replace("AM", "am")
        .replace("PM", "pm")
}

fun <T>Fragment.getNavigationResult(key: String = "result"): LiveData<T>? =
    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData(key)

fun <T>Fragment.setNavigationResult(result: T, key: String = "result") {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)
}

fun View.show() {
    if (visibility == VISIBLE) return

    val parent = parent as ViewGroup
    // View needs to be laid out to create a snapshot & know position to animate. If view isn't
    // laid out yet, need to do this manually.
    if (!isLaidOut) {
        measure(
            MeasureSpec.makeMeasureSpec(parent.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(parent.height, MeasureSpec.AT_MOST)
        )
        layout(parent.left, parent.height - measuredHeight, parent.right, parent.height)
    }

    val drawable = BitmapDrawable(context.resources, drawToBitmap())
    drawable.setBounds(left, parent.height, right, parent.height + height)
    parent.overlay.add(drawable)
    ValueAnimator.ofInt(parent.height, top).apply {
        startDelay = 100L
        duration = 300L
        interpolator = AnimationUtils.loadInterpolator(
            context,
            android.R.interpolator.linear_out_slow_in
        )
        addUpdateListener {
            val newTop = it.animatedValue as Int
            drawable.setBounds(left, newTop, right, newTop + height)
        }
        doOnEnd {
            parent.overlay.remove(drawable)
            visibility = VISIBLE
        }
        start()
    }
}

fun View.hide() {
    if (visibility == GONE) return

    val parent = parent as ViewGroup
    // View needs to be laid out to create a snapshot & know position to animate. If view isn't
    // laid out yet, need to do this manually.
    if (!isLaidOut) {
        measure(
            MeasureSpec.makeMeasureSpec(parent.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(parent.height, MeasureSpec.AT_MOST)
        )
        layout(parent.left, parent.height - measuredHeight, parent.right, parent.height)
    }

    val drawable = BitmapDrawable(context.resources, drawToBitmap())

    drawable.setBounds(left, top, right, bottom)
    parent.overlay.add(drawable)
    visibility = GONE
    ValueAnimator.ofInt(top, parent.height).apply {
        startDelay = 100L
        duration = 200L
        interpolator = AnimationUtils.loadInterpolator(
            context,
            android.R.interpolator.fast_out_linear_in
        )
        addUpdateListener {
            val newTop = it.animatedValue as Int
            drawable.setBounds(left, newTop, right, newTop + height)
        }
        doOnEnd {
            parent.overlay.remove(drawable)
        }
        start()
    }
}
