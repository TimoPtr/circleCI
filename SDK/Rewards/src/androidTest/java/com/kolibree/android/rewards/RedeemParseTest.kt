/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.rewards.synchronization.redeem.RedeemData
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RedeemParseTest : BaseMockWebServerTest<RewardsApi>() {
    override fun retrofitServiceClass(): Class<RewardsApi> = RewardsApi::class.java

    override fun context(): Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun parseResponse() {
        val jsonResponse = SharedTestUtils.getJson("redeem/redeem.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val redeemApi = retrofitService().claimRedeem(RedeemData(1, 1)).execute().body()!!

        assertEquals("https://kolibree.com", redeemApi.redeemUrl)
        assertEquals("Redeemed successfully", redeemApi.result)
        assertEquals(1L, redeemApi.rewardsId)
    }

    @Test
    fun parseResponseNoUrl() {
        val jsonResponse = SharedTestUtils.getJson("redeem/redeem_no_url.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val redeemApi = retrofitService().claimRedeem(RedeemData(1, 1)).execute().body()!!

        assertNull(redeemApi.redeemUrl)
        assertEquals("Redeemed successfully", redeemApi.result)
        assertEquals(1L, redeemApi.rewardsId)
    }
}
