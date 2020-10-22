package com.kolibree.android.network.token

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.kolibree.android.network.api.response.RefreshTokenResponse
import com.kolibree.android.network.models.RefreshTokenBody
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class TokenRefresherImplTest : BaseMockWebServerTest<TokenApi>() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun retrofitServiceClass(): Class<TokenApi> {
        return TokenApi::class.java
    }

    private lateinit var tokenRefresher: TokenRefresherImpl

    @Before
    override fun setUp() {
        super.setUp()
        tokenRefresher = TokenRefresherImpl(retrofitService())
    }

    /*
    REFRESH TOKEN V3
     */
    @Test
    fun refreshTokenV3_invokesApiWithAccessTokenAndRefreshTokenQuery() {
        val expectedTokenProvider = prepareRefreshTokenResponse()

        val accessToken = "sample_access_token"
        tokenRefresher.refreshToken(accessToken, refreshToken).test()
                .assertNoErrors()
                .assertValue(expectedTokenProvider)

        val recordedRequestUrl = getRefreshTokenRequestUrl()
        assertEquals(2, recordedRequestUrl.querySize())
        assertEquals(accessToken, recordedRequestUrl.queryParameter("access_token"))
        assertEquals(refreshToken, recordedRequestUrl.queryParameter("refresh_token"))
    }

    /*
    UTILS
     */

    private fun prepareRefreshTokenResponse(): RefreshTokenResponse {
        val jsonResponse = SharedTestUtils.getJson("json/account/request_token.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        return RefreshTokenResponse(jsonResponse)
    }

    private fun verifyRequest(email: String? = null, appId: String? = null, phoneNumber: String? = null) {
        val refreshTokenRequest = getRefreshTokenBodyFromRequest()

        assertEquals(email, refreshTokenRequest.email)
        assertEquals(appId, refreshTokenRequest.appId)
        assertEquals(phoneNumber, refreshTokenRequest.phoneNumber)
    }

    private fun getRefreshTokenBodyFromRequest(): RefreshTokenBody {
        val recordedRequest = mockWebServer.takeRequest()

        return Gson().fromJson(recordedRequest.body.readUtf8(), RefreshTokenBody::class.java)
    }

    private fun getRefreshTokenRequestUrl(): HttpUrl {
        val recordedRequest = mockWebServer.takeRequest()

        return recordedRequest.requestUrl
    }

    companion object {
        const val refreshToken = "REFRESH TOKEEEN"
    }
}
