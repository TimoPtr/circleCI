/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.pact.personalchallenge.post

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi
import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeRequest
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase.assertEquals
import okhttp3.OkHttpClient

class PersonalChallengeCreateErrorAlreadyExistsPactTest :
    PactBaseTest<PactProviderState.TestProfileWithPersonalChallenge>(
        PactProviderState.TestProfileWithPersonalChallenge
    ) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("create personal challenge fails if it already exists")
            .withPathAndDefaultHeader("/v1/rewards/personal-challenges/profile/${state.profileId}/")
            .method("POST")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .body(requestBody)
            .willRespondWith()
            .status(400)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            PersonalChallengeApi::class.java,
            okHttpClient = okHttpClient()
        )
        val response = client.createChallenge(
            state.profileId,
            defaultGson().fromJson<PersonalChallengeRequest>(
                requestBody,
                PersonalChallengeRequest::class.java
            )
        ).blockingGet()

        assertEquals(400, response.code())
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }

    private val requestBody =
        SharedTestUtils.getJson("personalchallenge/request_create_challenge.json")
}
