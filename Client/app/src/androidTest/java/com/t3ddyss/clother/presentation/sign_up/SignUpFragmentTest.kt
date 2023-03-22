package com.t3ddyss.clother.presentation.sign_up

import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import com.t3ddyss.clother.R
import com.t3ddyss.clother.presentation.auth.signup.SignUpFragment
import com.t3ddyss.clother.utils.EspressoUtil
import com.t3ddyss.clother.utils.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.*
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class SignUpFragmentTest {
    private val hiltAndroidRule = HiltAndroidRule(this)
    @get:Rule
    val rule = hiltAndroidRule

    private lateinit var navController: NavController

    @Before
    fun setUp() {
        launchFragmentInHiltContainer<SignUpFragment> {
            navController = TestNavHostController(ApplicationProvider.getApplicationContext())
            val navGraph = navController.navInflater.inflate(R.navigation.main_graph)
            navGraph.setStartDestination(R.id.signUpFragment)
            navController.graph = navGraph
            Navigation.setViewNavController(requireView(), navController)
        }
    }

    @Test
    fun signInTextClick_shouldNavigateToSignInFragment() {
        onView(withId(R.id.textView_sign_in))
            .perform(click())

        assertThat(navController.currentDestination?.id).isEqualTo(R.id.signInFragment)
    }

    @Test
    fun leavingEmailEmpty_shouldShowError() {
        onView(withId(R.id.editText_name))
            .perform(typeText("John"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.button_sign_up))
            .perform(click())

        val errorText = EspressoUtil.getString(R.string.auth_email_invalid)
        onView(withId(R.id.textInput_email))
            .check(matches(hasTextInputLayoutHintText(errorText)))
    }

    private fun hasTextInputLayoutHintText(expectedHintText: String): Matcher<View> =
        object : TypeSafeMatcher<View>() {

        override fun describeTo(description: Description?) { }

        override fun matchesSafely(item: View?): Boolean {
            if (item !is TextInputLayout) return false
            val error = item.error ?: return false

            val hint = error.toString()
            return expectedHintText == hint
        }
    }
}