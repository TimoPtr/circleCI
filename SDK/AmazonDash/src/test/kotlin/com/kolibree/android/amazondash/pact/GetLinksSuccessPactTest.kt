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
import com.kolibree.android.amazondash.data.model.AmazonDashGetLinkResponse
import com.kolibree.android.amazondash.data.remote.AmazonDashApi
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase.assertTrue
import okhttp3.OkHttpClient

internal class GetLinksSuccessPactTest : PactBaseTest<PactProviderState.TestProfileExists>(
    PactProviderState.TestProfileExists
) {
    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("get links succeeds with valid request")
            .withPathAndDefaultHeader("/v4/amazon/alexa_link/")
            .method("GET")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .willRespondWith()
            .body(defaultGson().toJson(AmazonDashGetLinkResponse("appUrl", "fallbackUrl")))
            .status(200)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            AmazonDashApi::class.java,
            okHttpClient = okHttpClient()
        )

        val response = client.getLinks().blockingGet()

        assertTrue(response.isSuccessful)
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }
}
