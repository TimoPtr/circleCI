/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.pact.challenges

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.rewards.synchronization.RewardsApi
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase
import okhttp3.OkHttpClient

internal class ChallengeProgressPactTest : PactBaseTest<PactProviderState.TestProfileExists>(
    PactProviderState.TestProfileExists
) {

    private val challengeProgressBody =
        SharedTestUtils.getJson("rewards/challenges/profile/list/challenge_progress.json")

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("fetch challenge progress for a profile")
            .withPathAndDefaultHeader("/v1/rewards/challenges/profile/list/${state.profileId}/")
            .method("GET")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .willRespondWith()
            .status(200)
            .body(challengeProgressBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            RewardsApi::class.java,
            okHttpClient = okHttpClient()
        )
        val response = client.getChallengeProgress(state.profileId).execute()

        val challengeProgressApi = response.body()

        TestCase.assertNotNull(challengeProgressApi)
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }
}
