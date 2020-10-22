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
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi
import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeRequest
import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeResponse
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import io.pactfoundation.consumer.dsl.LambdaDsl
import junit.framework.TestCase.assertEquals
import okhttp3.OkHttpClient
import org.threeten.bp.ZonedDateTime

class PersonalChallengeCreateWithSuccessPactTest :
    PactBaseTest<PactProviderState.TestProfileExists>(
        PactProviderState.TestProfileExists
    ) {

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("create personal challenge successfully")
            .withPathAndDefaultHeader("/v1/rewards/personal-challenges/profile/${state.profileId}/")
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

        assertEquals(201, response.code())
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

    private val requestBody =
        SharedTestUtils.getJson("personalchallenge/request_create_challenge.json")

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
