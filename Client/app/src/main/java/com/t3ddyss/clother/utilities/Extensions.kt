package com.t3ddyss.clother.utilities

import android.content.Context
import android.os.Build.FINGERPRINT
import android.text.Editable
import android.util.Patterns
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.abs
import kotlin.math.floor

val name_regex = """\p{L}{2,50}""".toRegex()
val password_regex = """^(?=.*?[0-9])(?=.*?[a-z])(?=.*?[A-Z])(?=\S+$)(?=.*?[^A-Za-z\s0-9]).{8,25}""".toRegex()

fun String.validateName() = this.matches(name_regex)

fun String.validateEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

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

fun LatLng.toCoordinatesString(precision: Int = 4): String {
    val latitude = convertToDms(this.latitude)
    val latitudeCardinal = if (this.latitude >= 0) "N" else "S"

    val longitude  = convertToDms(this.longitude)
    val longitudeCardinal = if (this.longitude >= 0) "E" else "W"
    return "$latitude$latitudeCardinal $longitude$longitudeCardinal"
}

fun convertToDms(coordinate: Double): String {
    val absolute = abs(coordinate)
    val degrees = floor(absolute).toInt()
    val minutesNotTruncated = (absolute - degrees) * 60
    val minutes = floor(minutesNotTruncated).toInt()
    val seconds = floor((minutesNotTruncated - minutes) * 60).toInt()
    return "$degrees°$minutes'$seconds\""
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun String.getImageUrlForCurrentDevice(): String {
    return if (this.startsWith("https://lp2.hm.com")) this
    else getBaseUrlForCurrentDevice() + this
}

fun isEmulator() = FINGERPRINT.contains("generic")

fun getBaseUrlForCurrentDevice() = if (isEmulator()) BASE_URL_EMULATOR else BASE_URL_DEVICE
