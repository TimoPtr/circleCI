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
import com.kolibree.android.questionoftheday.data.api.QuestionApi
import com.kolibree.android.questionoftheday.data.api.model.request.AnswerQuestionRequest
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase.assertTrue
import okhttp3.OkHttpClient
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

class SendAnswerPactTest : PactBaseTest<PactProviderState.TestProfileWithQuestionOfTheDay>(
    PactProviderState.TestProfileWithQuestionOfTheDay
) {

    private val body = AnswerQuestionRequest(
        id = 1,
        answerId = 1,
        answeredAt = OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
    )

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("send answer")
            .withPathAndDefaultHeader("/v1/rewards/${state.accountId}/${state.profileId}/question/")
            .method("PUT")
            .headers(
                mapOf(
                    "accept-language" to state.headerLanguage
                )
            )
            .body(defaultGson().toJson(body))
            .willRespondWith()
            .status(204)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            QuestionApi::class.java,
            okHttpClient = okHttpClient()
        )

        val response = client.sendAnswer(
            accountId = state.accountId,
            profileId = state.profileId,
            body = body
        ).blockingGet()

        assertTrue(response.isSuccessful)
    }

    private fun okHttpClient(): OkHttpClient {
        return defaultOkHttpBuilder()
            .addInterceptor(headerWithLanguageInterceptor(state.headerLanguage))
            .build()
    }
}
