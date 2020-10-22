/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.questionoftheday.pact

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.commons.ZONE_FORMATTER
import com.kolibree.android.questionoftheday.data.api.QuestionApi
import com.kolibree.android.questionoftheday.data.api.model.request.AnswerApiResponse
import com.kolibree.android.questionoftheday.data.api.model.request.QuestionApiResponse
import com.kolibree.android.questionoftheday.data.api.model.request.UserResponse
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.OkHttpClient
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

class FetchQuestionPactTest : PactBaseTest<PactProviderState.TestProfileWithQuestionOfTheDay>(
    PactProviderState.TestProfileWithQuestionOfTheDay
) {

    private val timezone = ZONE_FORMATTER.format(
        OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
    )

    private val answeredAt = OffsetDateTime.of(2020, 1, 1, 20, 0, 0, 0, ZoneOffset.UTC)

    private val expectedBody = QuestionApiResponse(
        id = 1,
        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
        correct = 1,
        answers = listOf(
            AnswerApiResponse(
                id = 1,
                text = "A"
            ),
            AnswerApiResponse(
                id = 2,
                text = "B"
            ),
            AnswerApiResponse(
                id = 3,
                text = "C"
            ),
            AnswerApiResponse(
                id = 4,
                text = "D"
            )
        ), userResponse = UserResponse(
            id = 1,
            answerId = 2,
            answeredAt = answeredAt
        )
    )

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("send answer")
            .withPathAndDefaultHeader("/v1/rewards/${state.accountId}/${state.profileId}/question/")
            .query("timezone=$timezone")
            .method("GET")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .willRespondWith()
            .body(defaultGson().toJson(expectedBody))
            .status(200)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            QuestionApi::class.java,
            okHttpClient = okHttpClient()
        )

        val response = client.fetchQuestion(
            accountId = state.accountId,
            profileId = state.profileId,
            timezone = timezone
        ).blockingGet()

        assertTrue(response.isSuccessful)
        assertEquals(expectedBody, response.body())
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }
}
