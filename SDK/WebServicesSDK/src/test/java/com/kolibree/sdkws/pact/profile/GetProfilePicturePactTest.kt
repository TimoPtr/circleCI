/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.pact.profile

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import com.kolibree.sdkws.profile.ProfileApi
import com.kolibree.sdkws.profile.models.PictureResponse
import io.pactfoundation.consumer.dsl.LambdaDsl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull

class GetProfilePicturePactTest :
    PactBaseTest<PactProviderState.TestProfileExists>(PactProviderState.TestProfileExists) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("Get Profile Picture")
            .path("/v3/accounts/${state.accountId}/profiles/${state.profileId}/")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(responseBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            ProfileApi::class.java
        )
        val response = client.getProfilePicture(state.accountId, state.profileId).blockingGet()

        assertEquals(200, response.code())
        assertNotNull(response.body())
        assertEquals(
            response.body(), defaultGson().fromJson(
                responseBody.toString(),
                PictureResponse::class.java
            )
        )
    }

    // Currently we're using the GetProfile API, which contains more fields than here.
    // https://kolibree.atlassian.net/browse/KLTB002-11553
    private val responseBody = LambdaDsl.newJsonBody {
        it.stringValue("picture_url", "test picture url")
            .stringValue("picture_last_modifier", "2020-06-10T06:23:26.190095")
    }.build()
}
