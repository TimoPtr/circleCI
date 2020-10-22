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
import com.kolibree.android.rewards.synchronization.profilesmiles.ProfileSmilesApi
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase
import junit.framework.TestCase.assertNotNull

class ProfileSmilesPactTest : PactBaseTest<PactProviderState.TestProfileWithSmilesExists>(
    PactProviderState.TestProfileWithSmilesExists
) {

    private val responseBody = SharedTestUtils.getJson("catalog/list/profile_smiles.json")

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("fetch profile smiles")
            .withPathAndDefaultHeader("/v1/rewards/smiles/profile/${state.profileId}/")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(responseBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(mockServer.getUrl(), RewardsApi::class.java)
        val response = client.getProfileSmiles(state.profileId).execute()

        val profileSmilesApi = response.body()!!

        assertNotNull(profileSmilesApi)

        val expectedProfileSmilesApi1 = ProfileSmilesApi(state.ownerProfileSmiles)

        TestCase.assertEquals(expectedProfileSmilesApi1, profileSmilesApi)
    }
}
