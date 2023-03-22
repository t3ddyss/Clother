package com.t3ddyss.core.util.utils

import androidx.core.util.PatternsCompat

object StringUtils {

    private const val STATUS_MAX_LENGTH = 70

    fun isValidName(name: String): Boolean {
        val regex = """[a-zA-Z\s]{2,50}""".toRegex()
        return name.matches(regex)
    }

    fun isValidEmail(email: String): Boolean {
        return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        val regex = """^(?=.*?\d)(?=.*?[a-z])(?=.*?[A-Z])(?=\S+$)(?=.*?[^A-Za-z\s\d]).{8,25}""".toRegex()
        return password.matches(regex)
    }

    fun isValidStatus(status: String): Boolean {
        return status.length <= STATUS_MAX_LENGTH
    }
}