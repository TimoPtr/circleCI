/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.sdkws.brushing.persistence.models

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.DATETIME_FORMATTER
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.sdkws.brushing.BrushingApi
import com.kolibree.sdkws.brushing.models.CreateMultipleBrushingSessionsBody
import com.kolibree.sdkws.data.model.CreateBrushingData
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.threeten.bp.OffsetDateTime

internal class BrushingInternalParseTest : BaseMockWebServerTest<BrushingApi>() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun retrofitServiceClass(): Class<BrushingApi> {
        return BrushingApi::class.java
    }

    @Test
    fun parseCreateBrushing() {
        val jsonResponse = SharedTestUtils.getJson("create_brushings_response.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val body = CreateMultipleBrushingSessionsBody(
            listOf(CreateBrushingData("", 1L, 1, TrustedClock.getNowOffsetDateTime(), 1))
        )

        val createdBrushings =
            retrofitService().createBrushings(
                1,
                2,
                body
            ).blockingGet().body()!!.getBrushings()

        assertEquals(2, createdBrushings.size)

        val firstBrushing = createdBrushings.first()

        val expectedTime: OffsetDateTime =
            OffsetDateTime.parse("2020-07-02T07:44:50+0200", DATETIME_FORMATTER)

        assertEquals(expectedTime, firstBrushing.dateTime)
    }
}
