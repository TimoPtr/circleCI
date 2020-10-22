/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.push

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase.assertEquals

internal class PushNotificationApiPactTest : PactBaseTest<PactProviderState.TestProfileExists>(
    PactProviderState.TestProfileExists
) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("upload Firebase token")
            .withPathAndDefaultHeader("/v3/accounts/${state.accountId}/deviceToken/")
            .method("POST")
            .body(defaultGson().toJson(PushNotificationTokenRequestBody(TOKEN, DEVICE_ID)))
            .willRespondWith()
            .status(204)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(mockServer.getUrl(), PushNotificationApi::class.java)
        val response = client.updatePushNotificationToken(
            state.accountId,
            PushNotificationTokenRequestBody(TOKEN, DEVICE_ID)
        ).blockingGet()

        assertEquals(204, response.code())
    }

    companion object {
        const val TOKEN = "firebase-token"
        const val DEVICE_ID = "unique-device-id"
    }
}
