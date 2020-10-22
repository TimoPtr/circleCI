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
import com.kolibree.sdkws.account.models.BrushSyncReminderResponse
import junit.framework.TestCase
import okhttp3.OkHttpClient

internal class GetBrushSyncReminderPactTest : PactBaseTest<PactProviderState.TestProfileExists>(
    PactProviderState.TestProfileExists
) {

    private val expectedBody = BrushSyncReminderResponse(isActive = true)

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("send answer")
            .withPathAndDefaultHeader("/v4/accounts/${state.accountId}/profiles/${state.profileId}/brush_sync_reminder/")
            .method("GET")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .willRespondWith()
            .body(defaultGson().toJson(expectedBody))
            .status(200)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            AccountApi::class.java,
            okHttpClient = okHttpClient()
        )

        val response = client.getBrushSyncReminderEnabled(
            accountId = state.accountId,
            profileId = state.profileId
        ).blockingGet()

        TestCase.assertTrue(response.isSuccessful)
        TestCase.assertEquals(200, response.code())
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }
}
