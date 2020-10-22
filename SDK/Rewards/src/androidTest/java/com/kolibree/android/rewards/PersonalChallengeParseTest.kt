/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.rewards.personalchallenge.data.api.PersonalChallengeApi
import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeRequest
import com.kolibree.android.rewards.personalchallenge.data.api.model.PersonalChallengeResponse
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.ZonedDateTime

@RunWith(AndroidJUnit4::class)
internal class PersonalChallengeParseTest : BaseMockWebServerTest<PersonalChallengeApi>() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun retrofitServiceClass(): Class<PersonalChallengeApi> =
        PersonalChallengeApi::class.java

    @Test
    fun testGetIncompleteChallengeResponse() {
        val jsonResponse =
            SharedTestUtils.getJson("personalchallenge/response_challenge_incomplete.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val response = retrofitService().getChallenge(1).blockingGet().body()!!

        val expected =
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

        assertEquals(response, expected)
    }

    @Test
    fun testGetCompleteChallengeResponse() {
        val jsonResponse =
            SharedTestUtils.getJson("personalchallenge/response_challenge_complete.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val response = retrofitService().getChallenge(1).blockingGet().body()!!

        val expected =
            PersonalChallengeResponse(
                id = 1,
                createdAt = ZonedDateTime.parse(
                    "2020-02-10T10:11:09+0000",
                    DATETIME_FORMATTER
                ),
                objective = "streak",
                level = "easy",
                duration = 7,
                durationUnit = "day",
                progress = 100,
                completedAt = ZonedDateTime.parse(
                    "2020-02-14T14:11:09+0000",
                    DATETIME_FORMATTER
                )
            )

        assertEquals(response, expected)
    }

    @Test
    fun testCreateChallengeResponse() {
        val request = Gson().fromJson<PersonalChallengeRequest>(
            SharedTestUtils.getJson("personalchallenge/request_create_challenge.json"),
            PersonalChallengeRequest::class.java
        )

        val jsonResponse =
            SharedTestUtils.getJson("personalchallenge/response_challenge_complete.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val response = retrofitService().createChallenge(1, request).blockingGet().body()!!

        val expected =
            PersonalChallengeResponse(
                id = 1,
                createdAt = ZonedDateTime.parse(
                    "2020-02-10T10:11:09+0000",
                    DATETIME_FORMATTER
                ),
                objective = "streak",
                level = "easy",
                duration = 7,
                durationUnit = "day",
                progress = 100,
                completedAt = ZonedDateTime.parse(
                    "2020-02-14T14:11:09+0000",
                    DATETIME_FORMATTER
                )
            )

        assertEquals(response, expected)
    }

    @Test
    fun testUpdateChallengeResponse() {
        val request = Gson().fromJson<PersonalChallengeRequest>(
            SharedTestUtils.getJson("personalchallenge/request_create_challenge.json"),
            PersonalChallengeRequest::class.java
        )

        val jsonResponse =
            SharedTestUtils.getJson("personalchallenge/response_challenge_complete.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val response = retrofitService().updateChallenge(1, request).blockingGet().body()!!

        val expected =
            PersonalChallengeResponse(
                id = 1,
                createdAt = ZonedDateTime.parse(
                    "2020-02-10T10:11:09+0000",
                    DATETIME_FORMATTER
                ),
                objective = "streak",
                level = "easy",
                duration = 7,
                durationUnit = "day",
                progress = 100,
                completedAt = ZonedDateTime.parse(
                    "2020-02-14T14:11:09+0000",
                    DATETIME_FORMATTER
                )
            )

        assertEquals(response, expected)
    }
}
