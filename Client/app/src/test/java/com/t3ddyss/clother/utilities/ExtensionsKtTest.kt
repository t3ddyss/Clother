package com.t3ddyss.clother.utilities

import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.t3ddyss.core.util.utils.StringUtils
import org.junit.Test

@SmallTest
class ExtensionsKtTest {

    @Test
    fun `empty name returns false`() {
        assertThat(StringUtils.isValidName("")).isFalse()
    }

    @Test
    fun `too short name returns false`() {
        assertThat(StringUtils.isValidName("a")).isFalse()
    }

    @Test
    fun `too long name returns false`() {
        assertThat(StringUtils.isValidName("a".repeat(51))).isFalse()
    }

    @Test
    fun `name with digit returns false`() {
        assertThat(StringUtils.isValidName("abcde5")).isFalse()
    }

    @Test
    fun `valid name returns true`() {
        assertThat(StringUtils.isValidName("John")).isTrue()
    }

    @Test
    fun `valid name with space returns true`() {
        assertThat(StringUtils.isValidName("John Doe")).isTrue()
    }

    @Test
    fun `empty email returns false`() {
        assertThat(StringUtils.isValidName("")).isFalse()
    }

    @Test
    fun `incorrect email returns false`() {
        assertThat(StringUtils.isValidEmail("abcde")).isFalse()
    }

    @Test
    fun `correct email returns true`() {
        assertThat(StringUtils.isValidEmail("abcde@gmail.com")).isTrue()
    }

    @Test
    fun `empty password returns false`() {
        assertThat(StringUtils.isValidPassword("")).isFalse()
    }

    @Test
    fun `too short password returns false`() {
        assertThat(StringUtils.isValidPassword("Abc12?")).isFalse()
    }

    @Test
    fun `too long password returns false`() {
        val password = "A".repeat(5) +
                "b".repeat(5) +
                "c".repeat(5) +
                "1".repeat(5) +
                "2".repeat(5) +
                "?".repeat(5)

        assertThat(StringUtils.isValidPassword(password)).isFalse()
    }

    @Test
    fun `weak password returns false`() {
        assertThat(StringUtils.isValidPassword("123456789")).isFalse()
    }

    @Test
    fun `strong password returns true`() {
        assertThat(StringUtils.isValidPassword("Abc12?Def")).isTrue()
    }
}