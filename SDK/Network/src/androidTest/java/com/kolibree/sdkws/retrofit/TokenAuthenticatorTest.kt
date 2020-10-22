package com.kolibree.sdkws.retrofit

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.internal.RefreshTokenProvider
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.API_CLIENT_ACCESS_TOKEN
import com.kolibree.android.network.core.AccessTokenManager
import com.kolibree.android.network.retrofit.TokenAuthenticator
import com.kolibree.android.network.token.TokenRefresher
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.test.dagger.LazyContainer
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.Protocol.HTTP_2
import okhttp3.Request.Builder
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString

@RunWith(AndroidJUnit4::class)
internal class TokenAuthenticatorTest {
    private val tokenRefresher = mock<TokenRefresher>()
    private val accessTokenManager = mock<AccessTokenManager>()
    private val accountDatastore = mock<AccountDatastore>()

    private val tokenAuthenticator = TokenAuthenticator(
        LazyContainer.create(tokenRefresher),
        LazyContainer.create(accessTokenManager),
        accountDatastore
    )

    @After
    fun tearDown() {
        FailEarly.overrideDelegateWith(TestDelegate)
    }

    private fun mockUpdateTokens(expectedTokenProvider: RefreshTokenProvider) {
        whenever(accessTokenManager.updateTokens(expectedTokenProvider))
            .thenAnswer { invocation ->
                Completable.fromAction {
                    val newTokenProvider = invocation.getArgument<RefreshTokenProvider>(0)

                    val mockedAccessToken = newTokenProvider.getAccessToken()

                    whenever(accessTokenManager.getAccessToken()).thenReturn(mockedAccessToken)
                }
            }
    }

    @Test
    fun onAuthenticationError_responseErrorCODE_EXPIRED_ACCESS_TOKEN_returnsRequestWithNewAccessTokenFromV3RefreshTokenApi() {
        val responseBody = SharedTestUtils.getJson("json/expired_access_token.json")

        val accessToken = "accessToken"
        mockStoredAccessToken(accessToken)
        val expectedToken = "TOKEN"
        mockAccount(refreshToken = expectedToken)

        val response = mockResponse(responseBody)

        val newAccessToken = "NEW_access_TOKEN"
        val expectedTokenProvider = mock<RefreshTokenProvider>()
        whenever(expectedTokenProvider.getAccessToken()).thenReturn(newAccessToken)

        whenever(
            tokenRefresher.refreshToken(
                accessToken = accessToken,
                refreshToken = expectedToken
            )
        ).thenReturn(
            Single.just(
                expectedTokenProvider
            )
        )

        mockUpdateTokens(expectedTokenProvider)

        val request = tokenAuthenticator.onAuthenticationError(response)

        assertNotNull(request)

        verify(tokenRefresher).refreshToken(accessToken, expectedToken)

        verify(accessTokenManager).updateTokens(expectedTokenProvider)

        assertEquals(
            newAccessToken,
            request!!.header(API_CLIENT_ACCESS_TOKEN)
        )
    }

    @Test
    fun onAuthenticationError_nullRefreshToken_returnsNull() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val responseBody = SharedTestUtils.getJson("json/expired_access_token.json")
        val response = mockResponseBuilder(responseBody).build()

        mockAccount(refreshToken = "", email = "da")

        val expectedTokenProvider = mock<RefreshTokenProvider>()
        whenever(tokenRefresher.refreshToken(anyString(), anyString())).thenReturn(
            Single.just(
                expectedTokenProvider
            )
        )

        assertRefreshFailed(response)
    }

    @Test
    fun onAuthenticationError_responseErrorINVALID_ACCESS_TOKEN_returnsNull() {
        val responseBody = SharedTestUtils.getJson("json/invalid_access_token.json")
        val response = mockResponseBuilder(responseBody).build()

        assertRefreshFailed(response)
    }

    @Test
    fun onAuthenticationError_thirdAttempt_returnsNull() {
        val responseBody = SharedTestUtils.getJson("json/expired_access_token.json")

        val priorResponse1 = mockResponseBuilder(null).build()

        val priorResponse2 = mockResponseBuilder(null)
            .priorResponse(priorResponse1)
            .build()

        val response = mockResponseBuilder(responseBody)
            .priorResponse(priorResponse2)
            .build()

        assertRefreshFailed(response)
    }

    private fun assertRefreshFailed(response: Response) {
        assertNull(tokenAuthenticator.onAuthenticationError(response))

        verify(accessTokenManager).notifyUnableToRefreshToken()
    }

    private fun mockResponse(responseBody: String): Response {
        return mockResponseBuilder(responseBody).build()
    }

    private fun mockResponseBuilder(responseBody: String?): Response.Builder {
        var builder = Response.Builder()
            .request(Builder().url("http://www.example.com").build())
            .protocol(HTTP_2)
            .code(401)
            .message("")

        if (responseBody != null)
            builder =
                builder.body(ResponseBody.create(MediaType.parse("application/json"), responseBody))

        return builder
    }

    private fun mockStoredAccessToken(accessToken: String?) {
        whenever(accessTokenManager.getAccessToken()).thenReturn(accessToken)
    }

    var account: AccountInternal? = null

    private fun mockAccount(
        refreshToken: String? = null,
        email: String? = null,
        phone: String? = null,
        appId: String? = null
    ): AccountInternal {
        if (account == null) {
            account = AccountInternal()
            whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(account))
        }

        account?.let {
            if (refreshToken != null)
                it.refreshToken = refreshToken

            if (email != null)
                it.email = email

            if (phone != null)
                it.phoneNumber = phone

            if (appId != null)
                it.appId = appId
        }

        return account!!
    }
}
