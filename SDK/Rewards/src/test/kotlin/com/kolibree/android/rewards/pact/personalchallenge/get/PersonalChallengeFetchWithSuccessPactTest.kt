/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.pact.personalchallenge.get

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi
import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeResponse
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import io.pactfoundation.consumer.dsl.LambdaDsl
import junit.framework.TestCase.assertEquals
import okhttp3.OkHttpClient
import org.threeten.bp.ZonedDateTime

class PersonalChallengeFetchWithSuccessPactTest :
    PactBaseTest<PactProviderState.TestProfileWithPersonalChallenge>(
        PactProviderState.TestProfileWithPersonalChallenge
    ) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("get personal challenge successfully")
            .withPathAndDefaultHeader("/v1/rewards/personal-challenges/profile/${state.profileId}/")
            .method("GET")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .willRespondWith()
            .status(200)
            .body(responseBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            PersonalChallengeApi::class.java,
            okHttpClient = okHttpClient()
        )
        val response = client.getChallenge(state.profileId).blockingGet()

        assertEquals(200, response.code())
        val challengeResponse = response.body()!!

        val expectedResponse =
            PersonalChallengeResponse(
                id = 1,
                createdAt = ZonedDateTime.parse(
                    "2020-02-14T14:11:09+0000",
                    DATETIME_FORMATTER
                ),
                objective = "streak",
                level = "easy",
                duration = 7,
                durationUnit = "day",
                progress = 25,
                completedAt = null
            )
        assertEquals(expectedResponse, challengeResponse)
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }

    private val responseBody = LambdaDsl.newJsonBody {
        it.numberValue("id", 1)
        it.stringValue("objective", "streak")
        it.stringValue("level", "easy")
        it.numberValue("duration_unit", 7)
        it.stringValue("duration_period", "day")
        it.numberValue("progress", 25)
        it.nullValue("completed_at")
        it.stringMatcher(
            "created_at",
            state.creationTimeShortRegexp,
            "2020-02-14T14:11:09+0000"
        )
    }.build()
}
