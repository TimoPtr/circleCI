/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate

@RunWith(AndroidJUnit4::class)
internal class PrizesCatalogParseTest : BaseMockWebServerTest<RewardsApi>() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun retrofitServiceClass(): Class<RewardsApi> {
        return RewardsApi::class.java
    }

    @Test
    fun parseResponse() {
        val jsonResponse = SharedTestUtils.getJson("catalog/list/prize_list.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val prizesCatalogInternal = retrofitService().getAllPrizes().execute().body()!!

        assertEquals(1, prizesCatalogInternal.prizes.size)

        val prizeApi = prizesCatalogInternal.prizes.first()

        assertEquals("rewards", prizeApi.category)
        assertEquals(1, prizeApi.categoryId)

        assertEquals(1, prizeApi.details.size)

        val prizeDetails = prizeApi.details.first()

        assertEquals(500, prizeDetails.smilesRequired)
        assertTrue(prizeDetails.purchasable)
        assertEquals(0.0, prizeDetails.voucherDiscount)

        val expectedDescription =
            "You want to get this new ship faster? Speed up your GoPirate progression with an in-game Gold Booster. This booster increases your GoPirate Gold by 1000."
        assertEquals(expectedDescription, prizeDetails.description)

        assertEquals("GoPirate Gold Amplifier", prizeDetails.title)

        assertEquals("kolibree", prizeDetails.company)
        assertEquals("https://dyg43gvcnlkrm.cloudfront.net/rewards/PirateBooster.png", prizeDetails.pictureUrl)

        assertEquals(LocalDate.parse("2020-02-13"), prizeDetails.creationDate)
        assertEquals(1, prizeDetails.rewardsId)
        assertNull(prizeDetails.productId)
    }
}
