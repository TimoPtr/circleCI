/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.pact.tiers

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.rewards.synchronization.profiletier.ProfileTierApi
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase
import junit.framework.TestCase.assertNotNull

class ProfileTierPactTest : PactBaseTest<PactProviderState.TestProfileExists>(
    PactProviderState.TestProfileExists
) {

    private val responseBody = SharedTestUtils.getJson("catalog/list/profile_tier.json")

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("fetch profile tier")
            .withPathAndDefaultHeader("/v1/rewards/tier/profile/${state.profileId}/")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(responseBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(mockServer.getUrl(), RewardsApi::class.java)
        val response = client.getProfileTier(state.profileId).execute()

        val profileTierApi = response.body()!!

        assertNotNull(profileTierApi)

        val expectedProfileTier = ProfileTierApi(
            tier = "Ivory",
            tierId = 1,
            pictureUrl = "https://dyg43gvcnlkrm.cloudfront.net/rewards/Tier_Ivory.png"
        )

        TestCase.assertEquals(expectedProfileTier, profileTierApi)
    }
}
