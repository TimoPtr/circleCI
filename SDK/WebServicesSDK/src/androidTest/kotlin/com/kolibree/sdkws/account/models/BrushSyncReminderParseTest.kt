/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.models

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.sdkws.account.AccountApi
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class BrushSyncReminderParseTest : BaseMockWebServerTest<AccountApi>() {

    override fun retrofitServiceClass(): Class<AccountApi> = AccountApi::class.java

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testGetBrushSyncReminder() {
        val jsonResponse = SharedTestUtils.getJson("brush_sync_reminder_response.json")
        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val body = retrofitService()
            .getBrushSyncReminderEnabled(
                accountId = 1,
                profileId = 1
            )
            .blockingGet()
            .body()

        val expectedBody = BrushSyncReminderResponse(isActive = true)
        assertEquals(expectedBody, body)
    }
}
