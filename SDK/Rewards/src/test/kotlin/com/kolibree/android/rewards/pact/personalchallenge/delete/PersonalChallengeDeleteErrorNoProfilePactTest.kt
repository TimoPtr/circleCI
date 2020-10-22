/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.pact.personalchallenge.delete

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase.assertEquals
import okhttp3.OkHttpClient

private const val INCORRECT_PROFILE_ID = 1234L

class PersonalChallengeUpdateErrorNoProfilePactTest :
    PactBaseTest<PactProviderState.TestProfileWithPersonalChallenge>(
        PactProviderState.TestProfileWithPersonalChallenge
    ) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("delete personal challenge fails for unknown profile")
            .withPathAndDefaultHeader("/v1/rewards/personal-challenges/profile/$INCORRECT_PROFILE_ID/")
            .method("DELETE")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .willRespondWith()
            .status(404)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            PersonalChallengeApi::class.java,
            okHttpClient = okHttpClient()
        )
        val response = client.deleteChallenge(INCORRECT_PROFILE_ID).blockingGet()

        assertEquals(404, response.code())
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }
}
