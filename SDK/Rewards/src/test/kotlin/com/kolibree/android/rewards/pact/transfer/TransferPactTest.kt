/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.pact.transfer

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.rewards.synchronization.transfer.TransferData
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import io.pactfoundation.consumer.dsl.LambdaDsl
import junit.framework.TestCase
import okhttp3.OkHttpClient

class TransferPactTest : PactBaseTest<PactProviderState.TwoProfileAccountExists>(
    PactProviderState.TwoProfileAccountExists
) {

    private val transferResponseBody = LambdaDsl.newJsonBody {
        it.numberValue("smiles", 101)
        it.stringValue("result", "Transferred successfully")
        it.numberValue("from_profile", state.profileId)
        it.numberValue("to_profile", state.secondProfileId)
    }.build()

    private val transferData = TransferData(101, state.profileId, state.secondProfileId)

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact =
        builder.given(state.backendName)
            .uponReceiving("transfer smiles")
            .withPathAndDefaultHeader("/v1/rewards/transfer/smiles/")
            .method("POST")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .body(defaultGson().toJson(transferData))
            .willRespondWith()
            .status(200)
            .body(transferResponseBody)
            .toPact()

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            RewardsApi::class.java,
            okHttpClient = okHttpClient()
        )
        val response = client.transferSmiles(transferData).execute()
        val transferApi = response.body()!!

        TestCase.assertEquals(101, transferApi.smiles)
        TestCase.assertEquals("Transferred successfully", transferApi.result)
        TestCase.assertEquals(state.profileId, transferApi.fromProfileId)
        TestCase.assertEquals(state.secondProfileId, transferApi.toProfileId)
    }

    private fun okHttpClient(): OkHttpClient = defaultOkHttpBuilder()
        .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
        .build()
}
