/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.data.api

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.game.gameprogress.data.api.model.GameProgressRequest
import com.kolibree.android.game.gameprogress.data.api.model.GameProgressResponse
import com.kolibree.android.game.gameprogress.data.api.model.ProfileGameProgressResponse
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.ZonedDateTime

@RunWith(AndroidJUnit4::class)
internal class GameProgressParseTest : BaseMockWebServerTest<GameProgressApi>() {

    override fun retrofitServiceClass(): Class<GameProgressApi> = GameProgressApi::class.java

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testGetProfileGameProgressResponse() {
        val jsonResponse = SharedTestUtils.getJson("gameprogress/response_get_profile_game_progress.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val response = retrofitService().getProfileGameProgress(1, 2).blockingGet().body()!!

        val expected = ProfileGameProgressResponse(
            1, 2, listOf(
                GameProgressResponse(
                    "hello", "world", ZonedDateTime.parse(
                        "2020-02-14T14:11:09+0000",
                        DATETIME_FORMATTER
                    )
                )
            )
        )

        assertEquals(expected, response)
    }

    @Test
    fun testGetGameProgressResponse() {
        val jsonResponse = SharedTestUtils.getJson("gameprogress/response_game_progress.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val response = retrofitService().getGameProgress(1, 2, "").blockingGet().body()!!

        val expected =
            GameProgressResponse(
                "hello", "world", ZonedDateTime.parse(
                    "2020-02-14T14:11:09+0000",
                    DATETIME_FORMATTER
                )
            )

        assertEquals(expected, response)
    }

    @Test
    fun testSetGameProgressResponse() {
        val jsonResponse = SharedTestUtils.getJson("gameprogress/response_game_progress.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val response = retrofitService().setGameProgress(1, 2, "", GameProgressRequest("")).blockingGet().body()!!

        val expected =
            GameProgressResponse(
                "hello", "world", ZonedDateTime.parse(
                    "2020-02-14T14:11:09+0000",
                    DATETIME_FORMATTER
                )
            )

        assertEquals(expected, response)
    }
}
