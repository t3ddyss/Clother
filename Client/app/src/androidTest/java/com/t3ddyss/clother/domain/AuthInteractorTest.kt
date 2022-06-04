package com.t3ddyss.clother.domain

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.InfoMessage
import com.t3ddyss.core.domain.models.Success
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@MediumTest
class AuthInteractorTest {

    private val hiltAndroidRule = HiltAndroidRule(this)
    private val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val rule: RuleChain = RuleChain
        .outerRule(hiltAndroidRule)
        .around(instantTaskExecutorRule)

    @Inject
    lateinit var mockWebServer: MockWebServer
    @Inject
    lateinit var authInteractor: AuthInteractor

    /**
     * MockWebServer starts implicitly in [com.t3ddyss.clother.di.TestNetworkModule]
     */
    @Before
    fun setUp() {
        hiltAndroidRule.inject()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun successfulLogin_shouldReturnTokensAndUser() = runTest {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(SUCCESSFUL_LOGIN_RESPONSE_BODY)
        mockWebServer.enqueue(response)

        val result = authInteractor.signIn("email", "password")
        val authData = result.content!!

        assertThat(result is Success<*>).isTrue()
        assertThat(authData.accessToken).isEqualTo("some_access_token")
        assertThat(authData.refreshToken).isEqualTo("some_refresh_token")
        assertThat(authData.user.details).isNull()
        assertThat(authData.user.id).isEqualTo(1)
        assertThat(authData.user.image).isEmpty()
        assertThat(authData.user.name).isEqualTo("John")
    }

    @Test
    fun unsuccessfulLogin_shouldReturnCause() = runTest {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            .setBody(UNSUCCESSFUL_LOGIN_RESPONSE_BODY)
        mockWebServer.enqueue(response)

        val result = authInteractor.signIn("email", "password")
        assertThat(result is Error<*>).isTrue()
        assertThat((result.message as InfoMessage.StringMessage).message).isEqualTo("Wrong email or password")
    }

    private companion object {
        const val SUCCESSFUL_LOGIN_RESPONSE_BODY = """{
            "access_token": "some_access_token",
            "refresh_token": "some_refresh_token",
            "user": {
                "email": "abcde@gmail.com",
                "id": 1,
                "image": null,
                "name": "John"
            }
        }"""

        const val UNSUCCESSFUL_LOGIN_RESPONSE_BODY = """{
            "message": "Wrong email or password"
        }"""
    }
}