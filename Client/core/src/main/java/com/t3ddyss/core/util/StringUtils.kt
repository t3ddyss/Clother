package com.t3ddyss.core.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.util.PatternsCompat
import com.t3ddyss.core.R
import java.net.ConnectException

object StringUtils {
    private val namePattern = """\p{L}{2,50}""".toRegex()
    private val passwordPattern =
        """^(?=.*?[0-9])(?=.*?[a-z])(?=.*?[A-Z])(?=\S+$)(?=.*?[^A-Za-z\s0-9]).{8,25}""".toRegex()

    fun isValidName(name: String): Boolean {
        return name.matches(namePattern)
    }

    fun isValidEmail(email: String): Boolean {
        return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.matches(passwordPattern)
    }

    fun getErrorText(throwable: Throwable?, context: Context): String {
        @StringRes
        val errorResId: Int = if (throwable != null) {
            when (throwable) {
                is ConnectException -> R.string.error_no_connection
                else -> R.string.error_unknown
            }
        } else {
            R.string.error_unknown
        }
        return context.getString(errorResId)
    }
}