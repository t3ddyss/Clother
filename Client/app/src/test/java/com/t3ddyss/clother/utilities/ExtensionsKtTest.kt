package com.t3ddyss.clother.utilities

import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.t3ddyss.clother.util.validateEmail
import com.t3ddyss.clother.util.validateName
import com.t3ddyss.clother.util.validatePassword
import org.junit.Test

@SmallTest
class ExtensionsKtTest {

    @Test
    fun `empty name returns false`() {
        assertThat("".validateName()).isFalse()
    }

    @Test
    fun `too short name returns false`() {
        assertThat("a".validateName()).isFalse()
    }

    @Test
    fun `too long name returns false`() {
        assertThat(("a".repeat(51)).validateName()).isFalse()
    }

    @Test
    fun `valid name returns true`() {
        assertThat("Fedor".validateName()).isTrue()
    }

    @Test
    fun `empty email returns false`() {
        assertThat("".validateEmail()).isFalse()
    }

    @Test
    fun `incorrect email returns false`() {
        assertThat("abcde".validateEmail()).isFalse()
    }

    @Test
    fun `correct email returns true`() {
        assertThat("abcde@gmail.com".validateEmail()).isTrue()
    }

    @Test
    fun `empty password returns false`() {
        assertThat("".validatePassword()).isFalse()
    }

    @Test
    fun `too short password returns false`() {
        assertThat("Abc12?".validatePassword()).isFalse()
    }

    @Test
    fun `too long password returns false`() {
        val password = "A".repeat(5) +
                "b".repeat(5) +
                "c".repeat(5) +
                "1".repeat(5) +
                "2".repeat(5) +
                "?".repeat(5)

        assertThat(password.validatePassword()).isFalse()
    }

    @Test
    fun `weak password returns false`() {
        assertThat("123456789".validatePassword()).isFalse()
    }

    @Test
    fun `strong password returns true`() {
        assertThat("Abc12?Def".validatePassword()).isTrue()
    }
}