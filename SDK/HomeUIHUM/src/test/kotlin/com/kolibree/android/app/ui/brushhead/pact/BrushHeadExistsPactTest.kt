/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.pact

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.app.ui.brushhead.api.BrushHeadInformationApi
import com.kolibree.android.app.ui.brushhead.api.model.request.data.BrushHeadData
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState.BrushHeadExists
import junit.framework.TestCase.assertTrue
import okhttp3.OkHttpClient
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset.UTC

/**
 * Pact test used for the "/v4/accounts/{accountId}/profiles/{profileId}/brushhead/" endpoint.
 * The POST & GET method send the same response
 */
class BrushHeadExistsPactTest : PactBaseTest<BrushHeadExists>(BrushHeadExists) {

    private val brushHeadData = BrushHeadData(
        firstUsed = OffsetDateTime.of(2010, 1, 1, 1, 1, 1, 0, UTC)
    )

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("send brush head replaced")
            .withPathAndDefaultHeader("/v4/accounts/${state.accountId}/profiles/${state.profileId}/brushhead/${state.serialNumber}/${state.macAddress}/")
            .method("POST")
            .headers(mapOf("accept-language" to state.headerLanguage))
            .body(defaultGson().toJson(brushHeadData))
            .willRespondWith()
            .status(204)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            BrushHeadInformationApi::class.java,
            okHttpClient = okHttpClient()
        )

        val response = client.updateBrushHead(
            accountId = state.accountId,
            profileId = state.profileId,
            serialNumber = BrushHeadExists.serialNumber,
            macAddress = StrippedMac.fromMac(BrushHeadExists.macAddress),
            body = brushHeadData
        ).blockingGet()

        assertTrue(response.isSuccessful)
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }
}
