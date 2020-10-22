/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.pact

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.shop.data.api.VoucherApi
import com.kolibree.android.shop.data.api.model.VoucherRequest
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import io.pactfoundation.consumer.dsl.LambdaDsl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import okhttp3.OkHttpClient
import org.junit.Ignore

@Ignore("Backend not ready")
class VoucherPactWithCancellationTest :
    PactBaseTest<PactProviderState.TestProfileExists>(PactProviderState.TestProfileExists) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("ask for voucher with cancellation")
            .withPathAndDefaultHeader("/v4/accounts/${state.accountId}/voucher/")
            .method("POST")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .body(requestBody)
            .willRespondWith()
            .status(201)
            .body(responseBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            VoucherApi::class.java,
            okHttpClient = okHttpClient()
        )
        val response = client.getVoucher(
            state.profileId,
            defaultGson().fromJson(
                requestBody,
                VoucherRequest::class.java
            )
        ).blockingGet()

        assertEquals(201, response.code())
        assertNotNull(response.body())
        assertTrue(response.body()!!.voucherCode.isNotEmpty())
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }

    private val requestBody =
        SharedTestUtils.getJson("voucher/request_voucher_with_cancellation.json")

    private val responseBody = LambdaDsl.newJsonBody {
        it.stringType("voucher_code")
    }.build()
}
