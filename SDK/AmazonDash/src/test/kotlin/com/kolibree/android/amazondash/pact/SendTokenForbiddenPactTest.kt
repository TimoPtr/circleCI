/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.pact

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.amazondash.data.model.AmazonDashSendTokenRequest
import com.kolibree.android.amazondash.data.remote.AmazonDashApi
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import okhttp3.OkHttpClient

internal class SendTokenForbiddenPactTest : PactBaseTest<PactProviderState.TestProfileExists>(
    PactProviderState.TestProfileExists
) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("send token fails when forbidden")
            .withPathAndDefaultHeader("/v4/accounts/${state.accountId}/amazon/auth/")
            .body(defaultGson().toJson(AmazonDashSendTokenRequest(FORBIDDEN_TEST_TOKEN)))
            .method("POST")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .willRespondWith()
            .status(403)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            AmazonDashApi::class.java,
            okHttpClient = okHttpClient()
        )

        val response = client.sendToken(
            accountId = state.accountId,
            body = AmazonDashSendTokenRequest(FORBIDDEN_TEST_TOKEN)
        ).blockingGet()

        assertFalse(response.isSuccessful)
        assertEquals(HTTP_FORBIDDEN, response.code())
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }
}

private const val FORBIDDEN_TEST_TOKEN = "invalidcode"
