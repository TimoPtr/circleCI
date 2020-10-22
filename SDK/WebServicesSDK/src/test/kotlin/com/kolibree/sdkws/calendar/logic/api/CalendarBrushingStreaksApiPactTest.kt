/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.calendar.logic.api

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.calendar.logic.api.model.BrushingStreaksResponse
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull

// API is not yet ready for this test
internal class CalendarBrushingStreaksApiPactTest :
    PactBaseTest<PactProviderState.TestProfileExists>(
        PactProviderState.TestProfileExists,
        ignore = true
    ) {

    private val responseBody = SharedTestUtils.getJson("calendar/brushing_streaks_set1.json")

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("fetch brushing streaks for a profile")
            .withPathAndDefaultHeader("/v1/rewards/streaks/profile/list/${state.profileId}/")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(responseBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(mockServer.getUrl(), BrushingStreaksApi::class.java)
        val response = client.getStreaksForProfile(state.profileId).blockingGet().body()

        assertNotNull(response)
        assertEquals(
            BrushingStreaksResponse.withDates(
                listOf(
                    listOf("2019-01-20", "2019-01-26"),
                    listOf("2019-01-27", "2019-02-02"),
                    listOf("2019-02-03", "2019-02-09")
                )
            ),
            response
        )
    }
}
