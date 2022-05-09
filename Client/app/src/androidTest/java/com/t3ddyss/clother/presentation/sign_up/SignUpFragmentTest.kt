package com.t3ddyss.clother.presentation.sign_up

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.textfield.TextInputLayout
import com.t3ddyss.clother.R
import com.t3ddyss.clother.presentation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class SignUpFragmentTest {
    private val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    val rule = hiltAndroidRule

    lateinit var activityScenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        activityScenario.close()
    }

    @Test
    fun signInTextClick_shouldNavigateToSignInFragment() {
        onView(withId(R.id.textView_sign_in))
            .perform(click())

        onView(withId(R.id.button_sign_in))
            .check(matches(isDisplayed()))
    }

    @Test
    fun leavingEmailEmpty_shouldShowError() {
        onView(withId(R.id.editText_name))
            .perform(typeText("John"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.button_sign_up))
            .perform(click())

        var expectedErrorText = ""
        activityScenario.onActivity { activity ->
            expectedErrorText = activity.getString(R.string.auth_email_invalid)
        }

        onView(withId(R.id.textInput_email))
            .check(matches(hasTextInputLayoutHintText(expectedErrorText)))
    }

    private fun hasTextInputLayoutHintText(expectedErrorText: String): Matcher<View> =
        object : TypeSafeMatcher<View>() {

        override fun describeTo(description: Description?) { }

        override fun matchesSafely(item: View?): Boolean {
            if (item !is TextInputLayout) return false
            val error = item.error ?: return false

            val hint = error.toString()
            return expectedErrorText == hint
        }
    }
}