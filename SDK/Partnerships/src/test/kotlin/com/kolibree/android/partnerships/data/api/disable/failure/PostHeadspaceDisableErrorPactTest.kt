/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.api.disable.failure

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.partnerships.data.api.PartnershipApi
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull

class PostHeadspaceDisableErrorPactTest :
    PactBaseTest<PactProviderState.TestProfileExists>(PactProviderState.TestProfileExists) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("Unlock Headspace")
            .path("/v1/rewards/account/${state.accountId}/profile/${state.profileId}/partner/headspace/disable/")
            .method("POST")
            .willRespondWith()
            .status(400)
            .body(expectedError)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            PartnershipApi::class.java
        )

        val response = client.disablePartnership(
            state.accountId,
            state.profileId,
            Partner.HEADSPACE.partnerName
        ).blockingGet()

        assertEquals(400, response.code())
        assertNull(response.body())
        assertNotNull(response.errorBody())
        val apiError = ApiError(response.errorBody()!!.string())
        assertEquals(400, apiError.httpCode)
        assertEquals(115, apiError.internalErrorCode)
        assertEquals("Something went wrong.", apiError.details)
    }

    private val expectedError = newJsonBody { root ->
        root.stringValue("display_message", "Something went wrong.")
            .stringValue("message", "E115: Something went wrong.")
            .numberValue("internal_error_code", 115)
            .stringValue("detail", "Something went wrong.")
            .numberValue("http_code", 400)
    }.build()
}
