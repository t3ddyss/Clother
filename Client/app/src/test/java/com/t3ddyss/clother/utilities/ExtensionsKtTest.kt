package com.t3ddyss.clother.utilities

import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
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
    fun `weak password returns false`() {
        assertThat("12345".validatePassword()).isFalse()
    }

    @Test
    fun `strong password returns true`() {
        assertThat("abc123ABC?".validatePassword()).isTrue()
    }
}