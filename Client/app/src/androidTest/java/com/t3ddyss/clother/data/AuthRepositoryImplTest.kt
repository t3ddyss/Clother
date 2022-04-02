package com.t3ddyss.clother.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.t3ddyss.clother.MainCoroutineRule
import com.t3ddyss.clother.data.auth.AuthRepositoryImpl
import com.t3ddyss.clother.data.remote.dto.AuthDataDto
import com.t3ddyss.core.domain.models.Error
import com.t3ddyss.core.domain.models.Success
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
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
class AuthRepositoryImplTest {

    private val hiltAndroidRule = HiltAndroidRule(this)
    private val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val rule = RuleChain
        .outerRule(hiltAndroidRule)
        .around(instantTaskExecutorRule)
        .around(mainCoroutineRule)

    @Inject
    lateinit var mockWebServer: MockWebServer
    @Inject
    lateinit var repositoryImpl: AuthRepositoryImpl

    @Before
    fun setUp() {
        hiltAndroidRule.inject()
        // MockWebServer starts implicitly in TestNetworkModule.kt
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun successfulLogin_shouldReturnTokensAndUser() = runBlocking {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(successfulLoginResponseBody)
        mockWebServer.enqueue(response)

        val result = repositoryImpl.signIn("email", "password")
        assertThat(result is Success<AuthDataDto>).isTrue()

        val authData = result.content
        assertThat(authData).isNotNull()
        assertThat(authData!!.accessToken).isEqualTo("some_access_token")
        assertThat(authData.refreshToken).isEqualTo("some_refresh_token")
        assertThat(authData.user.email).isEqualTo("abcde@gmail.com")
        assertThat(authData.user.id).isEqualTo(1)
        assertThat(authData.user.image).isNull()
        assertThat(authData.user.name).isEqualTo("John")
    }

    @Test
    fun unsuccessfulLogin_shouldReturnCause() = runBlocking {
        val response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
            .setBody(unsuccessfulLoginResponseBody)
        mockWebServer.enqueue(response)

        val result = repositoryImpl.signIn("email", "password")
        assertThat(result is Error<AuthDataDto>).isTrue()
        assertThat(result.message).isEqualTo("Wrong email or password")
    }
}

const val successfulLoginResponseBody = """{
    "access_token": "some_access_token",
    "refresh_token": "some_refresh_token",
    "user": {
        "email": "abcde@gmail.com",
        "id": 1,
        "image": null,
        "name": "John"
    }
}"""

const val unsuccessfulLoginResponseBody = """{
    "message": "Wrong email or password"
}"""