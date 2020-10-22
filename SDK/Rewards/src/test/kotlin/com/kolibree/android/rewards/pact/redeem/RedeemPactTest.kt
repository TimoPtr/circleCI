/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.pact.redeem

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.rewards.synchronization.redeem.RedeemData
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import io.pactfoundation.consumer.dsl.LambdaDsl
import junit.framework.TestCase.assertEquals
import okhttp3.OkHttpClient

class RedeemPactTest : PactBaseTest<PactProviderState.TestProfileWithSmilesExists>(
    PactProviderState.TestProfileWithSmilesExists
) {

    private val redeem = LambdaDsl.newJsonBody {
        it.stringValue("result", "Redeemed successfully")
        it.numberValue("rewards_id", 1)
    }.build()

    private val redeemData = RedeemData(1, state.profileId)

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact =
        builder.given(state.backendName)
            .uponReceiving("claim a redeem")
            .withPathAndDefaultHeader("/v1/rewards/redeem/smiles/")
            .method("POST")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .body(defaultGson().toJson(redeemData))
            .willRespondWith()
            .status(200)
            .body(redeem)
            .toPact()

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            RewardsApi::class.java,
            okHttpClient = okHttpClient()
        )
        val response = client.claimRedeem(redeemData).execute()
        val redeemApi = response.body()!!

        assertEquals("Redeemed successfully", redeemApi.result)
        assertEquals(1L, redeemApi.rewardsId)
    }

    private fun okHttpClient(): OkHttpClient = defaultOkHttpBuilder()
        .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
        .build()
}
