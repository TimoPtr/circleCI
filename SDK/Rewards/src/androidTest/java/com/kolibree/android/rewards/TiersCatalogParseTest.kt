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
import com.kolibree.android.rewards.synchronization.tiers.TierApi
import com.kolibree.android.rewards.synchronization.tiers.TiersCatalogApi
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate

@RunWith(AndroidJUnit4::class)
internal class TiersCatalogParseTest : BaseMockWebServerTest<RewardsApi>() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun retrofitServiceClass(): Class<RewardsApi> {
        return RewardsApi::class.java
    }

    @Test
    fun parseResponse() {
        val jsonResponse = SharedTestUtils.getJson("catalog/list/tiers_list.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val tiersApi: TiersCatalogApi = retrofitService().getTiersCatalog().execute().body()!!

        assertNotNull(tiersApi.tiers)
        assertEquals(5, tiersApi.tiers.size)

        val firstTier = TierApi(
            smilesPerBrushing = 1,
            challengesNeeded = 0,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Ivory.png",
            rank = "Ivory",
            creationDate = LocalDate.of(2020, 2, 13),
            message = "Congratulations! You reached Ivory tier"
        )

        val secondTier = TierApi(
            smilesPerBrushing = 2,
            challengesNeeded = 10,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Bronze.png",
            rank = "Bronze",
            creationDate = LocalDate.of(2020, 2, 13),
            message = "Congratulations! You reached Bronze tier"
        )

        val thirdTier = TierApi(
            smilesPerBrushing = 3,
            challengesNeeded = 20,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Silver.png",
            rank = "Silver",
            creationDate = LocalDate.of(2020, 2, 13),
            message = "Congratulations! You reached Silver tier"
        )

        val fourthTier = TierApi(
            smilesPerBrushing = 4,
            challengesNeeded = 30,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Gold.png",
            rank = "Gold",
            creationDate = LocalDate.of(2020, 2, 13),
            message = "Congratulations! You reached Gold tier"
        )

        val fifthTier = TierApi(
            smilesPerBrushing = 5,
            challengesNeeded = 40,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Platinum.png",
            rank = "Platinum",
            creationDate = LocalDate.of(2020, 2, 13),
            message = "Congratulations! You reached Platinum tier"
        )

        assertEquals(firstTier, tiersApi.tiers[1])
        assertEquals(secondTier, tiersApi.tiers[2])
        assertEquals(thirdTier, tiersApi.tiers[3])
        assertEquals(fourthTier, tiersApi.tiers[4])
        assertEquals(fifthTier, tiersApi.tiers[5])
    }
}
