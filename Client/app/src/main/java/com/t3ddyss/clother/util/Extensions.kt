package com.t3ddyss.clother.util

import android.content.Context
import android.text.Editable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun String.toBearer() = "Bearer $this"

fun TextInputEditText.text() = this.text.toString().trim()

fun Context.getThemeColor(@AttrRes res: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(res, typedValue, true)
    return typedValue.data
}

fun Int.toColorFilter() = BlendModeColorFilterCompat
    .createBlendModeColorFilterCompat(this, BlendModeCompat.SRC_ATOP)

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

fun LatLng.toCoordinatesString(): String {
    val doubleToDms: (Double) -> String = {
        val absolute = abs(it)
        val degrees = floor(absolute).toInt()
        val minutesNotTruncated = (absolute - degrees) * 60
        val minutes = floor(minutesNotTruncated).toInt()
        val seconds = floor((minutesNotTruncated - minutes) * 60).toInt()
        "$degrees°$minutes'$seconds\""
    }

    val latitude = doubleToDms(this.latitude)
    val latitudeCardinal = if (this.latitude >= 0) "N" else "S"
    val longitude = doubleToDms(this.longitude)
    val longitudeCardinal = if (this.longitude >= 0) "E" else "W"
    return "$latitude$latitudeCardinal $longitude$longitudeCardinal"
}

inline fun <T, R> Flow<List<T>>.nestedMap(crossinline mapper: (T) -> R): Flow<List<R>> {
    return this.map { it.map(mapper) }
}
