package com.t3ddyss.clother.presentation.sign_up

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.t3ddyss.clother.data.auth.AuthRepositoryImpl
import com.t3ddyss.clother.presentation.auth.SignUpViewModel
import com.t3ddyss.clother.utils.getOrAwaitValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@MediumTest
class SignUpViewModelTest {

    private val hiltAndroidRule = HiltAndroidRule(this)
    private val instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var viewModel: SignUpViewModel
    @Inject
    lateinit var repositoryImpl: AuthRepositoryImpl

    @get:Rule
    val rule = RuleChain
        .outerRule(hiltAndroidRule)
        .around(instantTaskExecutorRule)

    @Before
    fun setUp() {
        hiltAndroidRule.inject()
        viewModel = SignUpViewModel(repositoryImpl, SavedStateHandle())
    }

    @After
    fun tearDown() {

    }

    @Test
    fun createUserWithIncorrectName_shouldEnableNameError() = runBlockingTest {
        viewModel.createUserWithCredentials("?", "abcde@gmail.com", "12345Abc?")
        assertThat(viewModel.nameError.getOrAwaitValue()).isTrue()
    }

    @Test
    fun createUserWithCorrectName_shouldDisableNameError() = runBlockingTest {
        viewModel.createUserWithCredentials("John", "abcde@gmail.com", "12345Abc?")
        assertThat(viewModel.nameError.getOrAwaitValue()).isFalse()
    }
}