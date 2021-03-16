package com.t3ddyss.clother.utilities

import android.content.Context
import android.text.Editable
import android.util.Patterns
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.google.android.material.textfield.TextInputEditText

val name_regex = """\p{L}{2,50}""".toRegex()
val password_regex = """^(?=.*?[0-9])(?=.*?[a-z])(?=.*?[A-Z])(?=\S+$)(?=.*?[^A-Za-z\s0-9]).{8,25}""".toRegex()

fun String.validateName() = this.matches(name_regex)

fun String.validateEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.validatePassword() = this.matches(password_regex)

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun TextInputEditText.text() = this.text.toString().trim()

fun Context.getThemeColor(@AttrRes res: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute (res, typedValue, true)
    return typedValue.data
}
