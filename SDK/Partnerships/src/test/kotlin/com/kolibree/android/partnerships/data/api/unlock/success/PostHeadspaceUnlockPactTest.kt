/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.api.unlock.success

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.partnerships.data.api.PartnershipApi
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull

class PostHeadspaceUnlockPactTest :
    PactBaseTest<PactProviderState.TestProfileExists>(PactProviderState.TestProfileExists) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("Unlock Headspace")
            .path("/v1/rewards/account/${state.accountId}/profile/${state.profileId}/partner/headspace/unlock/")
            .method("POST")
            .willRespondWith()
            .status(200)
            .body(expectedResponseBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            PartnershipApi::class.java
        )
        val response = client.unlockPartnership(
            state.accountId,
            state.profileId,
            Partner.HEADSPACE.partnerName
        ).blockingGet()

        assertEquals(200, response.code())
        assertNotNull(response.body())
        assertEquals(Unit, response.body())
    }

    private val expectedResponseBody: DslPart = newJsonBody { }.build()
}
