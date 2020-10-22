/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.api.get.success

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.partnerships.data.api.PartnershipApi
import com.kolibree.android.partnerships.data.api.model.PartnershipResponse
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull

abstract class GetPartnershipStatePactTest :
    PactBaseTest<PactProviderState.TestProfileExists>(PactProviderState.TestProfileExists) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("Get Partnerships")
            .path("/v1/rewards/account/${state.accountId}/profile/${state.profileId}/partners/")
            .method("GET")
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
        val response = client.getPartnerships(state.accountId, state.profileId).blockingGet()

        assertEquals(200, response.code())
        assertNotNull(response.body())
        assertEquals(
            response.body(),
            defaultGson().fromJson(expectedResponseBody.toString(), PartnershipResponse::class.java)
        )

        validateApiResponse(response.body()!!)
    }

    protected abstract val expectedResponseBody: DslPart

    abstract fun validateApiResponse(body: PartnershipResponse)
}
