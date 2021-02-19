package com.t3ddyss.clother.ui.utils

import android.text.Editable
import android.util.Patterns
import com.google.android.material.textfield.TextInputEditText

val name_regex = "^(?=\\S{2,50}$)".toRegex()
val password_regex = "^(?=\\S{8,25}\$)(?=.*?\\d)(?=.*?[a-z])(?=.*?[A-Z])(?=\\S+\$)(?=.*?[^A-Za-z\\s0-9])".toRegex()

fun String.validateName(name: String) = name.matches(name_regex)

fun String.validateEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

fun String.validatePassword(password: String) = password.matches(password_regex)

fun String.toEditable():Editable = Editable.Factory.getInstance().newEditable(this)

fun TextInputEditText.text() = this.text.toString().trim()
