/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.pact.account

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import com.kolibree.sdkws.account.AccountApi
import com.kolibree.sdkws.account.models.VerifyUniqueNumberRequest
import com.kolibree.sdkws.account.models.VerifyUniqueNumberResponse
import io.pactfoundation.consumer.dsl.LambdaDsl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull

class CheckPhoneNumberAssociationPactTest :
    PactBaseTest<PactProviderState.TestProfileExists>(PactProviderState.TestProfileExists) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("Check phone number association")
                .path("/v4/accounts/phone-number-association/")
                .method("POST")
                .body(requestBody)
            .willRespondWith()
            .status(200)
            .body(responseBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            AccountApi::class.java
        )
        val response = client.checkPhoneNumberAssociation(
            defaultGson().fromJson(
                requestBody.toString(),
                VerifyUniqueNumberRequest::class.java
            )
        ).blockingGet()

        assertEquals(200, response.code())
        assertNotNull(response.body())
        assertEquals(response.body(), defaultGson().fromJson(
            responseBody.toString(),
            VerifyUniqueNumberResponse::class.java
        ))
    }

    private val requestBody = LambdaDsl.newJsonBody {
        it.stringValue("phone_number", "12345678901")
            .stringValue("verification_token", "testToken")
            .stringValue("verification_code", "testCode")
    }.build()

    private val responseBody = LambdaDsl.newJsonBody {
        it.booleanValue("phone_linked", true)
            .booleanValue("wechat_linked", false)
    }.build()
}
