/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.pact

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState.TestProfileExists
import com.kolibree.android.toothbrush.battery.data.BatteryLevelApi
import com.kolibree.android.toothbrush.battery.data.model.SendBatteryLevelRequest
import junit.framework.TestCase
import okhttp3.OkHttpClient

class SendBatteryLevelPactTest : PactBaseTest<TestProfileExists>(
    TestProfileExists
) {

    private val request = SendBatteryLevelRequest(
        macAddress = StrippedMac.fromMac("aa11bb22cc33"),
        serialNumber = "serialnumber",
        discreteLevel = 0
    )

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("send battery level")
            .withPathAndDefaultHeader("/v4/accounts/${state.accountId}/profiles/${state.profileId}/battery_level/")
            .method("POST")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .willRespondWith()
            .body(defaultGson().toJson(request))
            .status(200)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            BatteryLevelApi::class.java,
            okHttpClient = okHttpClient()
        )

        val response = client.sendBatteryLevel(
            accountId = state.accountId,
            profileId = state.profileId,
            body = request
        ).blockingGet()

        TestCase.assertTrue(response.isSuccessful)
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }
}
